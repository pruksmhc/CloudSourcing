Parse.Cloud.define('calculatePoints',function(request,response) {
  var today = new Date();
  var start = today.getMilliseconds();
  var rank = request.params.AnswerRank;
  var percent = request.params.Percentage;
  var points = null;

  switch(rank) {
    case 1:
      points = 100;
      break;
    case 2:
      points = 95;
      break;
    case 3:
      points = 90;
      break;
    default:
      points = percent;
  }

  if (points < 5) {
    points = 5;
  }

  var end = today.getMilliseconds();

  console.log("What's up");
  console.log(end-start);

  response.success(points);

 });

Parse.Cloud.job("deleteFlaggedClouds", function(request, status) {
  // Set up to modify user data
  Parse.Cloud.useMasterKey();
  // Query for all clouds. 
  var Clouds = Parse.Object.extend("Clouds");
  var query = new Parse.Query(Clouds);
  query.greaterThan("numReported", 10); //get clouds with more than 10 flags. 
  query.find({
      success:function(cloudToDelete) {
          // for each cloud to be deleted. 
          for(var i=0; i <cloudToDelete.length;i++){
          cloudToDelete[i].destroy(); //closing the success method. 
          }),
      error:function(error){
          status.error("There was this error"+error); 
      }
   }); //closing the query finds method. 
});  //closing the entire javascript method. 
     