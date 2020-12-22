// jshint esversion: 6
const {BeforeAll, Before, AfterAll} = require('cucumber');
const { spawn, execSync } = require('child_process');
const path = require('path');
const processArgs = require('yargs').argv;

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

const paramsToWorld = JSON.parse(processArgs["world-parameters"]);
const isLocalTest = (paramsToWorld.baseFunctionUri.includes('http://localhost') ?
  true : false);
/** 
 * Start up the azure function. The timeout has been set to 
 * a value that allows the function enough time to start up in
 * a ```npm test``` or via the vscode debug mode.
 */
BeforeAll({
  timeout: 400000
}, function () {
  if(!isLocalTest) {
    console.info("\nTest is not against a locally running function. Skipping function init.\n");
    return;
  }
  console.info("\nTest is against a locally running function. Starting function.\n");
  return new Promise((resolve, reject) => {
    const command = ( process.platform == 'win32' ? 'cmd.exe' : 'bash');
    const mavenScriptToRun = ( process.platform == 'win32' ? 'mvnw' : 'mvnw');
    const args = (
      process.platform == 'win32' ? 
      ['/c', mavenScriptToRun, '--offline', 'azure-functions:run'] :
      [mavenScriptToRun, '--offline', 'azure-functions:run']
    );
    const azFunctionSpawnConfig = {
      cwd: path.parse(process.cwd()).dir,
    };

    processes.useraccountservice.functionapp
      .processEventEmitter = new spawn(command, args,azFunctionSpawnConfig);
  
    processes.useraccountservice.functionapp
      .processEventEmitter.stdout.on('data', (data) => {
        if (/Host lock lease acquired by instance ID./.test(data)) {
          console.log(`Function App stdout: ${data}`);
          processes.useraccountservice.functionapp.promiseResolved = true;

          /* Close Process Streams as they are not needed */
          processes.useraccountservice.functionapp.processEventEmitter.stderr.end();
          processes.useraccountservice.functionapp.processEventEmitter.stdout.destroy();
          processes.useraccountservice.functionapp.processEventEmitter.stdin.destroy();

          resolve("Function App Ready!");
        } else if (/((Starting)|(istening))/.test(data)) {
          console.log(`Function App stdout: ${data}`);
        } else if (
          !processes.useraccountservice.functionapp.promiseResolved &&
          /ERROR\]?/.test(data)
        ) {
          reject(data);
        } else {
          console.log(`Function App stdout: ${data}`);
        }
      });
      
    processes.useraccountservice.functionapp
      .processEventEmitter.stderr.on('data', (data) => {
        if(/WARNING/.test(data)) {
          console.warn(`Function App warning: ${data}`)
        } else {
          console.error(`Function App stderr: ${data}`);
          reject(data);
        }
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
  if(!isLocalTest) {
    console.info("\nTest is not against a locally running function. Skipping function teardown.\n");
    return;
  }
  console.info("\nTest is against a locally running function. Tearing down the function.\n");
  return new Promise((resolve, reject) => {
    console.info(`\nTerminating the functions process running with id ${processes.useraccountservice.functionapp.processEventEmitter.pid}`);
    resolve(process.kill(processes.useraccountservice.functionapp.processEventEmitter.pid));
  })
  .finally(() => {
    processes.useraccountservice.functionapp.processEventEmitter.stderr.end();
    processes.useraccountservice.functionapp.processEventEmitter.stdout.destroy();
    processes.useraccountservice.functionapp.processEventEmitter.stdin.destroy();
    console.info("Attempting to clean-up any other processes using the function port");
    if(process.platform == 'win32') {
      const processName = execSync('netstat -ano | findstr :7071');
      const processId = (processName.toString().match(/(?:LISTENING\s+)(\d+)/))[1];
      execSync(`taskkill /F /PID ${processId}`);
    } else {
      execSync('kill $(lsof -t -i :7071)');
    }
  });
});
