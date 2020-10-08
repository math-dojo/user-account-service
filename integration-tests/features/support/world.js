// jshint esversion: 6
const { setWorldConstructor } = require("cucumber");

class CustomWorld {
    
  constructor({ attach, parameters }) {
    this.attach = attach;
    this.parameters = parameters;

    /* Prefix custom params with world namespace */
    this.world = {};
    this.world.request = {};
    this.world.request.headers = {};
    this.world.request.headers.apiKey = parameters.apiKey ? parameters.apiKey: "";
    this.world.request.headers["X-Api-Version"] = parameters.xApiVersion ? parameters.xApiVersion: "";
    this.world.request.body = {};
    this.world.response = new Promise((resolve, reject) => {});

    this.world.getFunctionBaseUri = () => parameters.baseFunctionUri;
  }
}

setWorldConstructor(CustomWorld);