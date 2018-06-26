angular.module('myApp.services', [])

.service ('alertSvc', function ($rootScope, $http, $interval, notificationSvc){
    // START A TIMER TO CALL THE BACKEND FOR MESSAGES
    var DELAY = 3000;  // default time in seconds to update locations

    // CHECK FOR ALERTS EVERY 'DELAY' SECONDS
    var startAlertTimer = function (){
        $interval( function () {
            console.log ('checking for alerts ...');
            // checkForAlerts();
            var promise = notificationSvc.getAllAlerts();
            promise.then (
                function (payload){
                    $rootScope.$broadcast ('SYSTEM_ALERT', payload.data);
                }                
            )
        }, DELAY);
    }

    var checkForAlerts = function (){

        var level = Math.floor(Math.random() * 10) + 1
        if (level > 7){
            var alertIndex = Math.floor(Math.random() * 3) + 0
            var data = messages[alertIndex];
            data.timestamp = new Date().getTime();
            $rootScope.$broadcast ('SYSTEM_ALERT', data);
        } 
    }
    return {
        startAlertTimer : startAlertTimer
    }
})

.service ('notificationSvc', function ($http){
    var baseurl = 'camel/notifications';

    var currentAlert = {};

    var setCurrentAlert = function (alertItem){
        currentAlert = alertItem;
    }

    var getCurrentAlert = function (){
        return currentAlert;
    }

    var getAllAlerts = function (){
        return $http.get(baseurl);
    }

    return {
        getAllAlerts : getAllAlerts,
        getCurrentAlert : getCurrentAlert,
        setCurrentAlert : setCurrentAlert
    }
})

.service ('accountSvc', function ($http){
    // MBAAS SERVICE FOR TESTING
    var baseurl = 'camel/accounts';

    var getAccountAll = function (){
        return $http.get(baseurl);
    }

    var getAccount = function (id){
        return $http.get(baseurl + '/' + id);
    }

    var createAccount = function (account) {
        var acctData = JSON.parse(JSON.stringify(account));
        delete acctData.Id;
        delete acctData.id;
        return $http.post (baseurl, acctData);
    }

    var updateAccount = function (account){
        // deep copy so we can remove the id field on the published data
        var acctData = JSON.parse(JSON.stringify(account));
        delete acctData.Id;
        return $http.put (baseurl + '/' + account.Id, acctData)
    }

    var deleteAccount = function (account){
        var acctID= account.Id;
        return $http.delete (baseurl + '/' + acctID, acctID);
    }

    return {
        getAccountAll   : getAccountAll,
        getAccount      : getAccount,
        createAccount   : createAccount,
        updateAccount   : updateAccount,
        deleteAccount   : deleteAccount
    }
        
})