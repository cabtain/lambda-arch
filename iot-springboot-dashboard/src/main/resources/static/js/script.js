/**
* 
**/

 var totalTrafficChartData={
            labels : ["Type"],
            datasets : [{
                label : "Equipment",
                data : [1]
            }
           ]
        };

var route37TrafficChartData={
            labels : ["Type"],
            datasets : [{
                data : [1]
            }
           ]
        };


jQuery(document).ready(function() {
    //Charts
    var ctx1 = document.getElementById("totalTrafficChart").getContext("2d");
    window.tChart = new Chart(ctx1, {
                type: 'bar',
                data: totalTrafficChartData
            });

    var ctx2 = document.getElementById("route37TrafficChart").getContext("2d");
    window.wChart = new Chart(ctx2, {
                type: 'doughnut',
                data: route37TrafficChartData
            });

    //tables
    var totalTrafficList = jQuery("#total_traffic");
    var windowTrafficList = jQuery("#window_traffic");

    //use sockjs
    var socket = new SockJS('/stomp');
    var stompClient = Stomp.over(socket);

    stompClient.connect({ }, function(frame) {
        //subscribe "/topic/trafficData" message
        stompClient.subscribe("/topic/trafficData", function(data) {
            var dataList = data.body;
            var resp=jQuery.parseJSON(dataList);

            //Total traffic
            var totalOutput='';
            jQuery.each(resp.totalTraffic, function(i,vh) {
                 totalOutput +="<tbody><tr><td>"+ vh.routeId+"</td><td>"+vh.vehicleType+"</td><td>"+vh.totalCount+"</td><td>"+(vh.totalSum/vh.totalCount).toFixed(2)+"</td><td>"+vh.timeStamp+"</td></tr></tbody>";
            });
            var t_tabl_start = "<table class='table table-bordered table-condensed table-hover innerTable'><thead><tr><th>Equipment</th><th>Type</th><th>Count</th><th>Average</th><th>Time</th></tr></thead>";
            var t_tabl_end = "</table>";
            totalTrafficList.html(t_tabl_start+totalOutput+t_tabl_end);

            //Window traffic
            var windowOutput='';
            jQuery.each(resp.windowTraffic, function(i,vh) {
                 windowOutput +="<tbody><tr><td>"+ vh.routeId+"</td><td>"+vh.vehicleType+"</td><td>"+vh.totalCount+"</td><td>"+(vh.totalSum/vh.totalCount).toFixed(2)+"</td><td>"+vh.timeStamp+"</td></tr></tbody>";
            });
            var w_tabl_start = "<table class='table table-bordered table-condensed table-hover innerTable'><thead><tr><th>Equipment</th><th>Type</th><th>Count</th><th>Average</th><th>Time</th></tr></thead>";
            var w_tabl_end = "</table>";
            windowTrafficList.html(w_tabl_start+windowOutput+w_tabl_end);

            //draw total traffic chart
            drawBarChart(resp.totalTraffic,totalTrafficChartData);
            window.tChart.update();

            //draw route-37 traffic chart
            drawDoughnutChart(resp.totalTraffic,route37TrafficChartData);
            window.wChart.update();

        });
    });
});

function drawBarChart(trafficDetail,trafficChartData){
    //Prepare data for total traffic chart
    var chartLabel = ["Temperature", "Current", "Voltage", "Vibration", "Level"];
    var routeName = ["Equipment_A", "Equipment_B", "Equipment_C"];
    var chartData0 =[0,0,0,0,0], chartData1 =[0,0,0,0,0], chartData2 =[0,0,0,0,0];

    jQuery.each(trafficDetail, function(i,vh) {

        if(vh.routeId == routeName[0]){
            chartData0.splice(chartLabel.indexOf(vh.vehicleType),1,(vh.totalSum/vh.totalCount).toFixed(2));
        }
        if(vh.routeId == routeName[1]){
            chartData1.splice(chartLabel.indexOf(vh.vehicleType),1,(vh.totalSum/vh.totalCount).toFixed(2));
        }
        if(vh.routeId == routeName[2]){
            chartData2.splice(chartLabel.indexOf(vh.vehicleType),1,(vh.totalSum/vh.totalCount).toFixed(2));
        }
    });

    var trafficData = {
        labels : chartLabel,
        datasets : [{
            label				  : routeName[0],
            borderColor           : "#878BB6",
            backgroundColor       : "#878BB6",
            data                  : chartData0
        },
        {
            label				  : routeName[1],
            borderColor           : "#4ACAB4",
            backgroundColor       : "#4ACAB4",
            data                  : chartData1
        },
        {
            label				  : routeName[2],
            borderColor           : "#FFEA88",
            backgroundColor       : "#FFEA88",
            data                  : chartData2
        }

        ]
    };
      //update chart
      trafficChartData.datasets=trafficData.datasets;
      trafficChartData.labels=trafficData.labels;
 }

function drawDoughnutChart(trafficDetail,trafficChartData){
    //Prepare data for Doughnut chart
    var chartData =[];
    var chartLabel = [];
    jQuery.each(trafficDetail, function(i,vh) {
        if(vh.routeId == "Equipment_A"){
            chartLabel.push(vh.vehicleType);
            chartData.push(vh.totalCount);
        }
      });
        var pieChartData = {
        labels : chartLabel,
        datasets : [{
            backgroundColor  : ["#E81574","#DDE815","#B315E8","#e9967a","#90ee90"],
            data             : chartData
        }]
    };

      //update chart
      trafficChartData.datasets=pieChartData.datasets;
      trafficChartData.labels=pieChartData.labels;
}

 function getRandomColor() {
    return  'rgba(' + Math.round(Math.random() * 255) + ',' + Math.round(Math.random() * 255) + ',' + Math.round(Math.random() * 255) + ',' + ('1') + ')';
};

