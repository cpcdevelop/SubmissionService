/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

$(document).ready(function(){
               
               $("#theform").submit(function(){
                   
                   var input = $('#workunits');
                   alert("pina"+input.val());
                   if (input.val() == "") {
                       alert('lofasz');
                       input.val('lofasz');
                       $("#error2").fadeIn(750);
                       return false;
                   }
               })
           });

