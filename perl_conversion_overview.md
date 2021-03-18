# Perl Conversion Overview

This markdown might not be complete and up-to-date but it'll give an overview on how we convert Perl-script into CASText.

Conversion stated here takes place in classes `PerlScript` and `PerlControlStructuresReplacer`.

## Conditions
<table>
<tr><th>Perl-Script</th><th>CASText</th></tr>
<tr>
	<td><pre>
if (CONDITION1) {BLOCK1} 
elsif (CONDITION2) {BLOCK2} 
else {BLOCK3}
</pre>
	</td><td><pre>
if (CONDITION1) then (BLOCK1) 
elseif (CONDITION2) then (BLOCK2) 
else (BLOCK3)
</pre>
	</td>
</tr><tr>
	<td>
		<pre>unless (CONDITION) {BLOCK}</pre>
	</td><td>
		<pre>if (not CONDITION) then (BLOCK)</pre>
	</td>
</tr>


<tr><th colspan=2>Loops (nested should work)</th></tr>
<tr>
	<td>
<pre>
do {BLOCK} while (CONDITION) 
</pre>
	</td><td>
<pre>
lcmdlbool x: true; 
while (x) do (BLOCK, x:CONDITION)
</pre>
	</td>
</tr><tr>
	<td>
<pre>
do {BLOCK} until (CONDITION) 
</pre>
	</td><td>
<pre>
lcmdlbool x: true; 
while (not x) do (BLOCK, x:CONDITION)
</pre>
	</td>
</tr>
<tr>
	<td>
<pre>
while (CONDITION) {BLOCK}
</pre>
	</td><td>
<pre>
while (CONDITION) do (BLOCK)
</pre>
	</td>
</tr><tr>
	<td>
<pre>
until (CONDITION) {BLOCK}
</pre>
	</td><td>
<pre>
unless (CONDITION) do (BLOCK)
</pre>
	</td>
</tr>
<tr>
	<td>
<pre>
for (INIT; CONDITION; COMMAND) {BLOCK}
</pre>
	</td><td>
<pre>
for (INIT) next (COMMAND) while (CONDITION) do (BLOCK)
</pre>
	</td>
</tr>
<tr>
	<td>
<pre>
for VAR (ARRAY) {BLOCK}
for (ARRAY) {BLOCK}
foreach VAR (ARRAY) {BLOCK}
foreach (ARRAY) {BLOCK}
</pre>
	</td><td>
<pre>
for VAR in (ARRAY) do (BLOCK)
</pre>
	</td>
</tr>
</table>

## Operators
<table>
<tr><th>Perl-Script</th><th>CASText</th></tr>
<tr>
	<td><pre>=</pre></td>
	<td><pre>:</pre></td>
</tr>
<tr>
	<td><pre>==</pre></td>
	<td><pre>=</pre></td>
</tr>
<tr>
	<td><pre>&&</pre></td>
	<td><pre>and</pre></td>
</tr>
<tr>
	<td><pre>||</pre></td>
	<td><pre>or</pre></td>
</tr>
<tr>
	<td><pre>a++</pre></td>
	<td><pre>(a:a+1)</pre></td>
</tr>
<tr>
	<td><pre>a--</pre></td>
	<td><pre>(a:a-1)</pre></td>
</tr>
<tr>
	<td><pre>x%y</pre></td>
	<td><pre>mod(x,y)</pre></td>
</tr>
<tr>
	<td><pre>x**y</pre></td>
	<td><pre>x^y</pre></td>
</tr>
<tr>
	<td><pre>x+=y</pre></td>
	<td><pre>x: x+y</pre></td>
</tr>
<tr>
	<td><pre>x-=y</pre></td>
	<td><pre>x: x-y</pre></td>
</tr>
<tr>
	<td><pre>x*=y</pre></td>
	<td><pre>x: x*y </pre></td>
</tr>
<tr>
	<td><pre>x/=y</pre></td>
	<td><pre>x: x/y</pre></td>
</tr>
<tr>
	<td><pre>x%=y</pre></td>
	<td><pre>x: mod(x,y)</pre></td>
</tr>
<tr>
	<td><pre>x**=y</pre></td>
	<td><pre>x: x^y</pre></td>
</tr>
<tr>
	<td><pre>#a</pre></td>
	<td><pre>(length(a)-1)</pre></td>
</tr>
<tr>
	<td><pre>x.=y</pre></td>
	<td><pre>x: sconcat(x, y)</pre></td>
</tr>
<tr>
	<td><pre>1..9</pre></td>
	<td><pre>makelist(i,i,1,9)</pre></td>
</tr>
</table>

## Functions
For functions with the same parameter list, we just replace functions name.
For different parameter list, we isolate parameters first and reorder for CASText.
<table>
<tr><th>Perl-Script</th><th>CASText</th></tr>
<tr><th colspan=2>Same Parameter List</th></tr>
<tr>
	<td><pre>&log()</pre></td>
	<td><pre>log(</pre></td>
</tr>
<tr>
	<td><pre>&exp()</pre></td>
	<td><pre>exp(</pre></td>
</tr>
<tr>
	<td><pre>&sqrt()</pre></td>
	<td><pre>sqrt(</pre></td>
</tr>
<tr>
	<td><pre>&abs()</pre></td>
	<td><pre>abs(</pre></td>
</tr>
<tr>
	<td><pre>&sgn()</pre></td>
	<td><pre>signum(</pre></td>
</tr>
<tr>
	<td><pre>&min()</pre></td>
	<td><pre>min(</pre></td>
</tr>
<tr>
	<td><pre>&max()</pre></td>
	<td><pre>max(</pre></td>
</tr>
<tr>
	<td><pre>&ceil()</pre></td>
	<td><pre>ceiling(</pre></td>
</tr>
<tr>
	<td><pre>&floor()</pre></td>
	<td><pre>floor(</pre></td>
</tr>
<tr>
	<td><pre>&factorial()</pre></td>
	<td><pre>factorial(</pre></td>
</tr>
<tr>
	<td><pre>&sin()</pre></td>
	<td><pre>sin(</pre></td>
</tr>
<tr>
	<td><pre>&cos()</pre></td>
	<td><pre>cos(</pre></td>
</tr>
<tr>
	<td><pre>&tan()</pre></td>
	<td><pre>tan(</pre></td>
</tr>
<tr>
	<td><pre>&asin()</pre></td>
	<td><pre>asin(</pre></td>
</tr>
<tr>
	<td><pre>&acos()</pre></td>
	<td><pre>acos(</pre></td>
</tr>
<tr>
	<td><pre>&atan()</pre></td>
	<td><pre>atan(</pre></td>
</tr>
<tr>
	<td><pre>&atan2()</pre></td>
	<td><pre>atan2(</pre></td>
</tr>
<tr>
	<td><pre>&sinh()</pre></td>
	<td><pre>sinh(</pre></td>
</tr>
<tr>
	<td><pre>&cosh()</pre></td>
	<td><pre>cosh(</pre></td>
</tr>
<tr>
	<td><pre>&tanh()</pre></td>
	<td><pre>tanh(</pre></td>
</tr>
<tr>
	<td><pre>&asinh()</pre></td>
	<td><pre>asinh(</pre></td>
</tr>
<tr>
	<td><pre>&acosh()</pre></td>
	<td><pre>acosh(</pre></td>
</tr>
<tr>
	<td><pre>&atanh()</pre></td>
	<td><pre>stanh(</pre></td>
</tr>
<tr>
	<td><pre>&to_string(</pre></td>
	<td><pre>sconcat(</pre></td>
</tr>
<tr>
	<td><pre>&sub_string(</pre></td>
	<td><pre>substring(</pre></td>
</tr>



<tr><th colspan=2>Different Parameter List</th></tr>
<tr>
	<td>
<pre>
VAR = &choose(INDEX, ARRAY)
</pre>
	</td><td>
<pre>
lcmdlarray: [ARRAY];
VAR: lcmdlarray[INDEX]
</pre>
	</td>
</tr>
<tr>
	<td>
<pre>
&cas("maxima", MAXIMA)
</pre>
	</td><td>
<pre>
block(MAXIMA)
</pre>
	</td>
</tr>
<tr>
	<td>
<pre>
&pow(x,y)
</pre>
	</td><td>
<pre>
(x^y)
</pre>
	</td>
</tr>
<tr>
	<td>
<pre>
&random_permutation(ARRAY)
&random_permutation(SEED, ARRAY)
</pre>
	</td><td>
<pre>
random_permutation(ARRAY)
</pre>
	</td>
</tr>
<tr>
	<td>
<pre>
&random_permutation(SEED, A,B,..)
</pre>
	</td><td>
<pre>
random_permutation([A,B,])
</pre>
	</td>
</tr>
<tr>
	<td>
<pre>
&random(LOWER, UPPER)
</pre>
	</td><td>
<pre>
rand_with_step(LOWER, UPPER, 1)
</pre>
	</td>
</tr>
<tr>
	<td>
<pre>
&random(LOWER, UPPER, STEP)
</pre>
	</td><td>
<pre>
rand_with_step(LOWER, UPPER, STEP)
</pre>
	</td>
</tr>
</table>


## Comments
Comments will be "preserved" in first place while converting. 
This way all other replacement won't affect comments.
Comments content is transfereed one-to-one into Maxima comments. 
In reverse conclusion this means Perl within comments will stay Perl.
<table>
<tr><th>Perl-Script</th><th>CASText</th></tr>
<tr>
	<td>
<pre>
#Test comment with \0 "backslash" \\ \n etc. \\\;
</pre>
	</td><td>
<pre>
/\*Test comment with \0 "backslash" \\ \n etc. \\\;\*/
</pre>
	</td>
</tr><tr>
	<td>
<pre>
#Test comment with Perl: &pow($x,$y)
</pre>
	</td><td>
<pre>
/\*Test comment with Perl: &pow($x,$y)\*/
</pre>
	</td>
</tr>
</table>


## Strings
Same as comments, strings will be "preserved" in first place.
But in contrast to comments variables within strings will be converted also.

These are some test-strings with quotes and backslashes.
<table>
<tr><th>Perl-Script</th><th>CASText</th></tr>
<tr><th colspan=2>Double quote</th></tr>
<tr>
	<td>
<pre>
$test1="test \backslash \"    .";
$test2="test \backslash \'    .";
$test3="String 'in' String 1  .";
$test4="String \"in\" String 2.";
</pre>
	</td><td>
<pre>
test1: "test \backslash \"    .";
test2: "test \backslash \'    .";
test3: "String 'in' String 1  .";
test4: "String \"in\" String 2.";
</pre>
	</td>
</tr>

<tr><th colspan=2>Single quote</th></tr>
<tr>
	<td>
<pre>
$test5='test \backslash \"    .';
$test6='test \backslash \'    .';
$test7='String "in" String 1  .';
$test8='String \'in\' String 2.';
</pre>
	</td><td>
<pre>
test5: "test \\backslash \\"    .";
test6: "test \\backslash \\'    .";
test7: "String "in" String 1  .";
test8: "String \\'in\\' String 2.";
</pre>
	</td>
</tr>
</table>
Here we can see problems with `test5` and `test7`. 
But this will throw an error in STACK, so you can fix it in Moodle at the latest.


<!--
<table>
<tr><td colspan=2>Title</td></tr>
<tr>
	<td>
<pre>

</pre>
	</td><td>
<pre>

</pre>
	</td>
</tr>

<tr>
	<td><pre></pre></td>
	<td><pre></pre></td>
</tr>
</table>
-->


