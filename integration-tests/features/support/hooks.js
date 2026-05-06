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
const isLocalTest = (paramsToWorld.baseFunctionUri.includes('http://localhost') ||
  paramsToWorld.baseFunctionUri.includes('http://127.0.0.1') ?
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

    let stdoutBuffer = '';
    let stderrBuffer = '';

    const handleLine = (line) => {
      if (/Host lock lease acquired by instance ID./.test(line)) {
        console.log(`Function App: ${line}`);
        processes.useraccountservice.functionapp.promiseResolved = true;

        /* Close Process Streams as they are not needed */
        processes.useraccountservice.functionapp.processEventEmitter.stderr.end();
        processes.useraccountservice.functionapp.processEventEmitter.stdout.destroy();
        processes.useraccountservice.functionapp.processEventEmitter.stdin.destroy();

        resolve("Function App Ready!");
      } else if (/((Starting)|(istening))/.test(line)) {
        console.log(`Function App: ${line}`);
      } else if (
        !processes.useraccountservice.functionapp.promiseResolved &&
        /ERROR\]?/.test(line)
      ) {
        reject(line);
      } else {
        console.log(`Function App: ${line}`);
      }
    };

    processes.useraccountservice.functionapp
      .processEventEmitter.stdout.on('data', (data) => {
        stdoutBuffer += data.toString();
        const lines = stdoutBuffer.split('\n');
        stdoutBuffer = lines.pop(); // keep the incomplete last chunk
        lines.forEach(handleLine);
      });
      
    processes.useraccountservice.functionapp
      .processEventEmitter.stderr.on('data', (data) => {
        stderrBuffer += data.toString();
        const lines = stderrBuffer.split('\n');
        stderrBuffer = lines.pop();
        lines.forEach(line => {
          if(/WARNING/.test(line)) {
            console.warn(`Function App warning: ${line}`);
          } else {
            console.error(`Function App stderr: ${line}`);
            reject(line);
          }
        });
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
    try {
      processes.useraccountservice.functionapp.processEventEmitter.stderr.end();
      processes.useraccountservice.functionapp.processEventEmitter.stdout.destroy();
      processes.useraccountservice.functionapp.processEventEmitter.stdin.destroy();
    } catch (e) {
      console.info("Streams already closed, skipping stream teardown.");
    }
    console.info("Attempting to clean-up any other processes using the function port");
    try {
      if(process.platform == 'win32') {
        const processName = execSync('netstat -ano | findstr :7071');
        const processId = (processName.toString().match(/(?:LISTENING\s+)(\d+)/))[1];
        execSync(`taskkill /F /PID ${processId}`);
      } else {
        // Use -sTCP:LISTEN to only match the process listening on 7071,
        // not HTTP clients (including this Node process) connected to it.
        execSync('kill $(lsof -t -i :7071 -sTCP:LISTEN)');
      }
    } catch (e) {
      console.info(`Port cleanup completed (${e.message})`);
    }
  });
});
