'use strict';

angular.module('myApp.activity', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/activity', {
    templateUrl: 'activity/activity.html',
    controller: 'activityCtrl'
  });
}])

.controller('activityCtrl', ['$scope', '$uibModal', 'accountSvc', 'alertSvc', function ($scope, $uibModal, accountSvc,alertSvc){

    // current account reference
    $scope.account = {};

    // on entering get all accounts
    var promise = accountSvc.getAccountAll();
    promise.then (
        function (payload){
            $scope.accounts = payload.data;
        }
    )

    // This code handles the account panel (expand and collapse)
    $scope.opened = false;
    $scope.singleModel = null;
    $scope.isCollapsed = true;

    // this handles the drop downs in the account panel
    $scope.data = {
        selectedData : null,
        selectedType : null
    }

    
    // called when the user clicks the add button
    $scope.addAccount = function (){
           $scope.account = {
                'Type': 'OTHER',
                'TickerSymbol': '',
                'AccountSource': 'OTHER',
                'id': new Date().getTime().toString(),
                'AnnualRevenue': 0,
                'Name': 'Enter Name ...',
                'AccountNumber': 0,
                'Active__c': 'NO'
        };
        $scope.isCollapsed = !$scope.isCollapsed;
        $scope.data.selectedData = 'OTHER';
        $scope.data.selectedType = 'OTHER';
        $scope.directive = 'insert';
    }

    // Called when the user clicks the edit button
    $scope.editAccount = function (){
        if ($scope.selectedRow < 0) {
            alert ('Please select a item in from the table below.')
        } else {
            $scope.directive = 'update';
            $scope.isCollapsed = !$scope.isCollapsed
            $scope.data.selectedData = $scope.account.AccountSource;
            $scope.data.selectedType = $scope.account.Type;
        }
    }


    // handle drop down changes for account source
    $scope.sourceChanged = function(value){
        $scope.account.AccountSource = value;
    }

    // handle drop down changes for account type
    $scope.typeChanged = function (value){
        $scope.account.Type = value;
    }

    // user clicked the save button
    $scope.saveRecord = function (){
        $scope.isCollapsed = !$scope.isCollapsed;
        if ($scope.directive === 'update'){
            // call update service
            var promise = accountSvc.updateAccount($scope.account);
            promise.then (
                function (payload){
                    console.log(payload);
                }
            )            
        } else if ($scope.directive === 'insert'){
            // call insert service
            var promise = accountSvc.createAccount($scope.account);
            promise.then (
                function (payload){
                    console.log(payload);
                    $scope.accounts.push ($scope.account);
                }
            )            
        }
    }

    // user clicked the cancel button
    $scope.cancel = function (){
        $scope.isCollapsed = !$scope.isCollapsed;
    }

    // This code manages the selection and highlighting for the table of transactions
    $scope.selectedRow = -1;
    $scope.setSelectedRow = function (index) {
        $scope.selectedRow = index;
        $scope.account = $scope.accounts[index];
    };

    $scope.getDisplayRevenue = function (item) {
        return item.AnnualRevenue && item.AnnualRevenue != 0 ? '$' + (item.AnnualRevenue/1000).toFixed(2) : "";
    }

    // show 'prettier' TYPE values in the table view
    $scope.getTypeDisplay = function(item) {
        return item.Type;
    }

    // show 'prettier' SOURCE values in the table view
    $scope.getSourceDisplay = function (item) {
        return item.AccountSource;
    }

    // Modal DELETE confirmation
    var modalInstance = null;
    $scope.deleteAccount = function (){
        if ($scope.selectedRow < 0) {
            alert ('Please select a item in from the table below.')
            return;
        }
        var acctName = $scope.accounts[$scope.selectedRow].Name;
        var account = $scope.accounts[$scope.selectedRow]
        modalInstance = $uibModal.open ({
            templateUrl : 'activity/modal.html',
            controller : 'deleteCtrl',
            resolve : {
                account : function () {
                    return account;
                }
            }
        }).closed.then(function(){
            // refresh the data
            var promise = accountSvc.getAccountAll();
            promise.then (
                function (payload){
                    $scope.accounts = payload.data;
                }
            )
          });
    }
}])

// Controller for the Modal Dialog - ugly!!
.controller ('deleteCtrl', ['$scope', '$uibModalInstance', 'account', 'accountSvc', function ($scope, $uibModalInstance, account, accountSvc){
    console.log ('in deleteCtrl');
    $scope.title1 = account.Name;
    $scope.close = function (){
        $uibModalInstance.dismiss ('CANCEL');
        console.log ('Ignore deletion');
    }
    $scope.deleteRecord = function (){
        console.log ('Deleting ' + account.Name);
        $uibModalInstance.dismiss ('OKAY');
        // CALL THE DELETE SERVICE
        var promise = accountSvc.deleteAccount(account);
        promise.then (
            function (payload){
                console.log(payload);
            }
        )            

    }
}]);