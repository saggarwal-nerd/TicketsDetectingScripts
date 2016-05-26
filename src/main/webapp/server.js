/**
 * http://usejsdoc.org/
 */
var shelljs = require('shelljs');

shelljs.exec('java -cp bin detector.AnnotationFinal', function (code, output) {
   console.log(output);
});