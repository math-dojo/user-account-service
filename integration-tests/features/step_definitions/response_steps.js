// jshint esversion: 6
const { Then } = require('cucumber');
const chai = require("chai");
const chaiAsPromised = require("chai-as-promised");

chai.use(chaiAsPromised);
const expect = chai.expect;

const { payloads } = require('../support/payloads');

Then(/I should get a status code (\d{3})/, {
    timeout: 60000
},function (expectedCode) {
    return Promise.all([
        expect(this.world.response).to.eventually.have.deep.own.
            property('status', Number.parseInt(expectedCode))
    ]);
});

Then(/the response should be a superset of all the keys and values set from \'(\w+)\'/, function (nameOfSchemaOfExpectedContents) {
    return Promise.all([
        expect(this.world.response).to.eventually.satisfy(function(response) {
            return expect(response.data).to.deep.include(payloads[nameOfSchemaOfExpectedContents]);
          })
    ]);
});

Then(/the response should have no body/, function () {
    return Promise.all([
        expect(this.world.response).to.eventually.have.deep.own.
            property('data', "")
    ]);
});

Then(/the response should contain a key \'(\w+)\' with value \'(\w+)\'/, function (keyName, expectedValue) {
    return Promise.all([
        expect(this.world.response).to.eventually.satisfy(function(response) {
            return expect(response.data).to.deep.include({[keyName]: expectedValue});
          })
    ]);
});