//Object of Array
var Name1=[]; var Age1=[];
function multipleUser()
{
Name1.push(document.getElementById("Name").value);
Age1.push(document.getElementById("Age").value);
function Person(name, age)
 {
    this.name = name;
    this.age = age;
  }
  var person1 = new Person(Name1,Age1);
  p2.innerHTML=JSON.stringify(person1);
}
//Push in an array
function array()
{
  var array1=[];
  array1.push(document.getElementById("Name").value);
  array1.push(document.getElementById("Age").value);
  document.write(array1);
}
//push in to a single object
function single()
{
var object={ };
object['name']=document.getElementById("Name").value;
object['age']=document.getElementById("Age").value; 
document.write(JSON.stringify(object));
document.write(object.name);
document.write(object.age);
}
//Array of Objects
var Students=[];
function multipleStudents()
{
     var Student=
      { 
        name:document.getElementById("Name").value,
        age:document.getElementById("Age").value
      };
       Students.push(Student);
       p2.innerHTML=JSON.stringify(Students);
}