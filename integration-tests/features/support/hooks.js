// jshint esversion: 6
const {BeforeAll, Before, AfterAll} = require('cucumber');
const { spawn, execSync } = require('child_process');
const path = require('path');

let processes = {};
processes.useraccountservice = {
    functionapp: {
      processEventEmitter: undefined,
      promiseResolved: false
    },
    database: {
      processEventEmitter: undefined,
      promise: new Promise((resolve, reject) => {})
    }
};

/** 
 * Start up the azure function. The timeout has been set to 
 * a value that allows the function enough time to start up in
 * a ```npm test``` or via the vscode debug mode.
 */
BeforeAll({
  timeout: 300000
}, function () {
  return new Promise((resolve, reject) => {
    const command = ( process.platform == 'win32' ? 'cmd.exe' : 'bash');
    const mavenScriptToRun = ( process.platform == 'win32' ? 'mvnw.cmd' : 'mvnw');
    const args = [mavenScriptToRun, '--offline', 'azure-functions:run'];
    const azFunctionSpawnConfig = {
      cwd: path.parse(process.cwd()).dir,
    };

    processes.useraccountservice.functionapp
      .processEventEmitter = new spawn(command, args,azFunctionSpawnConfig);
  
    processes.useraccountservice.functionapp
      .processEventEmitter.stdout.on('data', (data) => {
        if( /Application started\. Press Ctrl\+C to shut down./.test(data)) {
          console.log(`Function App stdout: ${data}`);
          processes.useraccountservice.functionapp.promiseResolved = true;

          /* Close Process Streams as they are not needed */
          processes.useraccountservice.functionapp.processEventEmitter.stderr.end();
          processes.useraccountservice.functionapp.processEventEmitter.stdout.destroy();
          processes.useraccountservice.functionapp.processEventEmitter.stdin.destroy();

          resolve("Function App Ready!");
        } else if(/((Starting)|(istening))/.test(data)){
          console.log(`Function App stdout: ${data}`);
        } else if(
          (!processes.useraccountservice.functionapp.promiseResolved) && 
          /ERROR\]?/.test(data)
        ){
          reject(data);
        } else {
          console.log(`Function App stdout: ${data}`);
        }
      });
      
    processes.useraccountservice.functionapp
      .processEventEmitter.stderr.on('data', (data) => {
        console.error(`Function App stderr: ${data}`);
        reject(data);
      });
  })
  .catch(err => {
    console.error(`Encountered error while starting up the function app: ${err}`);
    console.info('Attempting to kill running function app process if set');

    const terminationResult = processes.useraccountservice.functionapp.processEventEmitter.kill();

    if(!terminationResult) {
      console.error("Function process termination failed");
    } else {
      console.info("Function process terminated");
    }
    process.exit();
  });

});

AfterAll(function() {
  return new Promise((resolve, reject) => {
    console.info(`\nTerminating the functions process running with id ${processes.useraccountservice.functionapp.processEventEmitter.pid}`);
    resolve(process.kill(processes.useraccountservice.functionapp.processEventEmitter.pid));
  })
  .finally(() => {
    processes.useraccountservice.functionapp.processEventEmitter.stderr.end();
    processes.useraccountservice.functionapp.processEventEmitter.stdout.destroy();
    processes.useraccountservice.functionapp.processEventEmitter.stdin.destroy();
    execSync('kill $(lsof -t -i :7071)');
  });
});

Before(function() {
  // reset request and response objects
  this.world.request = {};
  this.world.request.headers = {};
  this.world.request.body = {};
  this.world.response = new Promise((resolve, reject) => {});

});