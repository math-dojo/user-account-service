var {BeforeAll, Before} = require('cucumber');
const { spawn } = require('child_process');

// Synchronous
BeforeAll(function () {
  // start the user account service
});

Before(function() {
  // reset request and response objects
  this.world.request = {};
  this.world.request.headers = {}
  this.world.request.body = {};
  this.world.response = new Promise((resolve, reject) => {});

});