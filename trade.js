var request = require('request');
var async = require('async');
var teamId = '4b606b42-d70f-4960-bd1d-da3aaff52355';
var apiUrl = 'http://128.199.74.105:2015';

module.exports = function executeTrade(pair, quantity) {
  async.waterfall([
    function(callback) {
      request.post({
        url: apiUrl + '/fx/quote',
        json: {teamId: teamId, currencyPair: pair, quantity: quantity}
      }, function(err, res, body) {
        console.log('Quoted price is', body.quoteResponse.fxRate);
        callback(null, body.quoteResponse.quoteId);
      });
    },function(quoteId, callback) {
      request.post({
        url: apiUrl + '/fx/quote/execute',
        json: {teamId: teamId, quoteId: quoteId}
      }, function(err, res, body) {
        callback(null, 'done');
      });
    }
  ], function(err, result) {
    request(apiUrl + '/account/balance/4b606b42-d70f-4960-bd1d-da3aaff52355',
      function(err, res, body) {
        console.log('Remaining balance:');
        console.log(body);
      });
  });
}