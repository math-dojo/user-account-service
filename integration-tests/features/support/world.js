// jshint esversion: 6
const { setWorldConstructor } = require("cucumber");

class CustomWorld {
    
  constructor({ attach, parameters }) {
    this.attach = attach;
    this.parameters = parameters;

    /* Prefix custom params with world namespace */
    this.world = {};
    this.world.processes = {};
    this.world.processes.useraccountservice = {
        functionapp: new Promise((resolve, reject) => {}),
        database: new Promise((resolve, reject) => {})
    };
    this.world.request = {};
    this.world.request.headers = {};
    this.world.request.body = {};
    this.world.response = new Promise((resolve, reject) => {});

    this.world.getFunctionBaseUri = () => "http://localhost:7071/api";
  }
}

setWorldConstructor(CustomWorld);