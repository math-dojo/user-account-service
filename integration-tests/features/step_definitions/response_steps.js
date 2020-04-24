// jshint esversion: 6
const { Then } = require('cucumber');
const { expect } = require("chai");

const { payloads } = require('../support/payloads');

Then(/I should get a status code (\d{3})/, function (expectedCode) {
    return this.world.response.then((response) => {
        expect(response).has.property("status");
        expect(response.status).to.be.equal(Number.parseInt(expectedCode));
    });
});

Then(/the response should contain all the keys and values set from \'(\w+)\'/, function (nameOfSchemaOfExpectedContents) {
    return this.world.response.then((response) => {
        expect(response).has.property("data");
        expect(response.data).contains(payloads[nameOfSchemaOfExpectedContents]);
    });
});
