# Prism Changelog

## 1.15.0 (2018-06-16)

### New components

-   **Template Tookit 2** ([#1418](https://github.com/PrismJS/prism/issues/1418)) [[`e063992`](https://github.com/PrismJS/prism/commit/e063992)]
-   **XQuery** ([#1411](https://github.com/PrismJS/prism/issues/1411)) [[`e326cb0`](https://github.com/PrismJS/prism/commit/e326cb0)]
-   **TAP** ([#1430](https://github.com/PrismJS/prism/issues/1430)) [[`8c2b71f`](https://github.com/PrismJS/prism/commit/8c2b71f)]

### Updated components

-   **HTTP** \* Absolute path is a valid request uri ([#1388](https://github.com/PrismJS/prism/issues/1388)) [[`f6e81cb`](https://github.com/PrismJS/prism/commit/f6e81cb)]
-   **Kotlin**
    _ Add keywords of Kotlin and modify it's number pattern. ([#1389](https://github.com/PrismJS/prism/issues/1389)) [[`1bf73b0`](https://github.com/PrismJS/prism/commit/1bf73b0)]
    _ Add `typealias` keyword ([#1437](https://github.com/PrismJS/prism/issues/1437)) [[`a21fdee`](https://github.com/PrismJS/prism/commit/a21fdee)]
-   \_\_JavaScript
    _ Improve Regexp pattern [[`5b043cf`](https://github.com/PrismJS/prism/commit/5b043cf)]
    _ Add support for one level of nesting inside template strings. Fix [#1397](https://github.com/PrismJS/prism/issues/1397) [[`db2d0eb`](https://github.com/PrismJS/prism/commit/db2d0eb)]
-   **Elixir** \* Elixir: Fix attributes consuming punctuation. Fix [#1392](https://github.com/PrismJS/prism/issues/1392) [[`dac0485`](https://github.com/PrismJS/prism/commit/dac0485)]
-   **Bash** \* Change reserved keyword reference ([#1396](https://github.com/PrismJS/prism/issues/1396)) [[`b94f01f`](https://github.com/PrismJS/prism/commit/b94f01f)]
-   **PowerShell** \* Allow for one level of nesting in expressions inside strings. Fix [#1407](https://github.com/PrismJS/prism/issues/1407) [[`9272d6f`](https://github.com/PrismJS/prism/commit/9272d6f)]
-   **JSX**
    _ Allow for two levels of nesting inside JSX tags. Fix [#1408](https://github.com/PrismJS/prism/issues/1408) [[`f1cd7c5`](https://github.com/PrismJS/prism/commit/f1cd7c5)]
    _ Add support for fragments short syntax. Fix [#1421](https://github.com/PrismJS/prism/issues/1421) [[`38ce121`](https://github.com/PrismJS/prism/commit/38ce121)]
-   **Pascal** \* Add `objectpascal` as an alias to `pascal` ([#1426](https://github.com/PrismJS/prism/issues/1426)) [[`a0bfc84`](https://github.com/PrismJS/prism/commit/a0bfc84)]
-   **Swift** \* Fix Swift 'protocol' keyword ([#1440](https://github.com/PrismJS/prism/issues/1440)) [[`081e318`](https://github.com/PrismJS/prism/commit/081e318)]

### Updated plugins

-   **File Highlight**
    _ Fix issue causing the Download button to show up on every code blocks. [[`cd22499`](https://github.com/PrismJS/prism/commit/cd22499)]
    _ Simplify lang regex on File Highlight plugin ([#1399](https://github.com/PrismJS/prism/issues/1399)) [[`7bc9a4a`](https://github.com/PrismJS/prism/commit/7bc9a4a)]
-   **Show Language** \* Don't process language if block language not set ([#1410](https://github.com/PrismJS/prism/issues/1410)) [[`c111869`](https://github.com/PrismJS/prism/commit/c111869)]
-   **Autoloader** \* ASP.NET should require C# [[`fa328bb`](https://github.com/PrismJS/prism/commit/fa328bb)]
-   **Line Numbers** \* Make line-numbers styles more specific ([#1434](https://github.com/PrismJS/prism/issues/1434), [#1435](https://github.com/PrismJS/prism/issues/1435)) [[`9ee4f54`](https://github.com/PrismJS/prism/commit/9ee4f54)]

### Updated themes

-   Add .token.class-name to rest of themes ([#1360](https://github.com/PrismJS/prism/issues/1360)) [[`f356dfe`](https://github.com/PrismJS/prism/commit/f356dfe)]

### Other changes

-   **Website**
    _ Site now loads over HTTPS!
    _ Use HTTPS / canonical URLs ([#1390](https://github.com/PrismJS/prism/issues/1390)) [[`95146c8`](https://github.com/PrismJS/prism/commit/95146c8)]
    _ Added Angular tutorial link [[`c436a7c`](https://github.com/PrismJS/prism/commit/c436a7c)]
    _ Use rel="icon" instead of rel="shortcut icon" ([#1398](https://github.com/PrismJS/prism/issues/1398)) [[`d95f8fb`](https://github.com/PrismJS/prism/commit/d95f8fb)]
    _ Fix Download page not handling multiple dependencies when from Redownload URL [[`c2ff248`](https://github.com/PrismJS/prism/commit/c2ff248)]
    _ Update documentation for node & webpack usage [[`1e99e96`](https://github.com/PrismJS/prism/commit/1e99e96)]
-   Handle optional dependencies in `loadLanguages()` ([#1417](https://github.com/PrismJS/prism/issues/1417)) [[`84935ac`](https://github.com/PrismJS/prism/commit/84935ac)]
-   Add Chinese translation [[`f2b1964`](https://github.com/PrismJS/prism/commit/f2b1964)]

## 1.14.0 (2018-04-11)

### New components

-   **GEDCOM** ([#1385](https://github.com/PrismJS/prism/issues/1385)) [[`6e0b20a`](https://github.com/PrismJS/prism/commit/6e0b20a)]
-   **Lisp** ([#1297](https://github.com/PrismJS/prism/issues/1297)) [[`46468f8`](https://github.com/PrismJS/prism/commit/46468f8)]
-   **Markup Templating** ([#1367](https://github.com/PrismJS/prism/issues/1367)) [[`5f9c078`](https://github.com/PrismJS/prism/commit/5f9c078)]
-   **Soy** ([#1387](https://github.com/PrismJS/prism/issues/1387)) [[`b4509bf`](https://github.com/PrismJS/prism/commit/b4509bf)]
-   **Velocity** ([#1378](https://github.com/PrismJS/prism/issues/1378)) [[`5a524f7`](https://github.com/PrismJS/prism/commit/5a524f7)]
-   **Visual Basic** ([#1382](https://github.com/PrismJS/prism/issues/1382)) [[`c673ec2`](https://github.com/PrismJS/prism/commit/c673ec2)]
-   **WebAssembly** ([#1386](https://github.com/PrismJS/prism/issues/1386)) [[`c28d8c5`](https://github.com/PrismJS/prism/commit/c28d8c5)]

### Updated components

-   **Bash**: \* Add curl to the list of common functions. Close [#1160](https://github.com/PrismJS/prism/issues/1160) [[`1bfc084`](https://github.com/PrismJS/prism/commit/1bfc084)]
-   **C-like**: \* Make single-line comments greedy. Fix [#1337](https://github.com/PrismJS/prism/issues/1337). Make sure [#1340](https://github.com/PrismJS/prism/issues/1340) stays fixed. [[`571f2c5`](https://github.com/PrismJS/prism/commit/571f2c5)]
-   **C#**:
    _ More generic class-name highlighting. Fix [#1365](https://github.com/PrismJS/prism/issues/1365) [[`a6837d2`](https://github.com/PrismJS/prism/commit/a6837d2)]
    _ More specific class-name highlighting. Fix [#1371](https://github.com/PrismJS/prism/issues/1371) [[`0a95f69`](https://github.com/PrismJS/prism/commit/0a95f69)]
-   **Eiffel**: \* Fix verbatim strings. Fix [#1379](https://github.com/PrismJS/prism/issues/1379) [[`04df41b`](https://github.com/PrismJS/prism/commit/04df41b)]
-   **Elixir** \* Make regexps greedy, remove comment hacks. Update known failures and tests. [[`e93d61f`](https://github.com/PrismJS/prism/commit/e93d61f)]
-   **ERB**: \* Make highlighting work properly in NodeJS ([#1367](https://github.com/PrismJS/prism/issues/1367)) [[`5f9c078`](https://github.com/PrismJS/prism/commit/5f9c078)]
-   **Fortran**: \* Make single-line comments greedy. Update known failures and tests. [[`c083b78`](https://github.com/PrismJS/prism/commit/c083b78)]
-   **Handlebars**: \* Make highlighting work properly in NodeJS ([#1367](https://github.com/PrismJS/prism/issues/1367)) [[`5f9c078`](https://github.com/PrismJS/prism/commit/5f9c078)]
-   **Java**: \* Add support for generics. Fix [#1351](https://github.com/PrismJS/prism/issues/1351) [[`a5cf302`](https://github.com/PrismJS/prism/commit/a5cf302)]
-   **JavaScript**:
    _ Add support for constants. Fix [#1348](https://github.com/PrismJS/prism/issues/1348) [[`9084481`](https://github.com/PrismJS/prism/commit/9084481)]
    _ Improve Regex matching [[`172d351`](https://github.com/PrismJS/prism/commit/172d351)]
-   **JSX**: \* Fix highlighting of empty objects. Fix [#1364](https://github.com/PrismJS/prism/issues/1364) [[`b26bbb8`](https://github.com/PrismJS/prism/commit/b26bbb8)]
-   **Monkey**: \* Make comments greedy. Update known failures and tests. [[`d7b2b43`](https://github.com/PrismJS/prism/commit/d7b2b43)]
-   **PHP**: \* Make highlighting work properly in NodeJS ([#1367](https://github.com/PrismJS/prism/issues/1367)) [[`5f9c078`](https://github.com/PrismJS/prism/commit/5f9c078)]
-   **Puppet**: \* Make heredoc, comments, regexps and strings greedy. Update known failures and tests. [[`0c139d1`](https://github.com/PrismJS/prism/commit/0c139d1)]
-   **Q**: \* Make comments greedy. Update known failures and tests. [[`a0f5081`](https://github.com/PrismJS/prism/commit/a0f5081)]
-   **Ruby**: \* Make multi-line comments greedy, remove single-line comment hack. Update known failures and tests. [[`b0e34fb`](https://github.com/PrismJS/prism/commit/b0e34fb)]
-   **SQL**: \* Add missing keywords. Fix [#1374](https://github.com/PrismJS/prism/issues/1374) [[`238b195`](https://github.com/PrismJS/prism/commit/238b195)]

### Updated plugins

-   **Command Line**: \* Command Line: Allow specifying output prefix using data-filter-output attribute. ([#856](https://github.com/PrismJS/prism/issues/856)) [[`094d546`](https://github.com/PrismJS/prism/commit/094d546)]
-   **File Highlight**: \* Add option to provide a download button, when used with the Toolbar plugin. Fix [#1030](https://github.com/PrismJS/prism/issues/1030) [[`9f22952`](https://github.com/PrismJS/prism/commit/9f22952)]

### Updated themes

-   **Default**: \* Reach AA contrast ratio level ([#1296](https://github.com/PrismJS/prism/issues/1296)) [[`8aea939`](https://github.com/PrismJS/prism/commit/8aea939)]

### Other changes

-   Website: Remove broken third-party tutorials from homepage [[`0efd6e1`](https://github.com/PrismJS/prism/commit/0efd6e1)]
-   Docs: Mention `loadLanguages()` function on homepage in the nodeJS section. Close [#972](https://github.com/PrismJS/prism/issues/972), close [#593](https://github.com/PrismJS/prism/issues/593) [[`4a14d20`](https://github.com/PrismJS/prism/commit/4a14d20)]
-   Core: Greedy patterns should always be matched against the full string. Fix [#1355](https://github.com/PrismJS/prism/issues/1355) [[`294efaa`](https://github.com/PrismJS/prism/commit/294efaa)]
-   Crystal: Update known failures. [[`e1d2d42`](https://github.com/PrismJS/prism/commit/e1d2d42)]
-   D: Update known failures and tests. [[`13d9991`](https://github.com/PrismJS/prism/commit/13d9991)]
-   Markdown: Update known failures. [[`5b6c76d`](https://github.com/PrismJS/prism/commit/5b6c76d)]
-   Matlab: Update known failures. [[`259b6fc`](https://github.com/PrismJS/prism/commit/259b6fc)]
-   Website: Remove non-existent anchor to failures. Reword on homepage to make is less misleading. [[`8c0911a`](https://github.com/PrismJS/prism/commit/8c0911a)]
-   Website: Add link to Keep Markup plugin in FAQ [[`e8cb6d4`](https://github.com/PrismJS/prism/commit/e8cb6d4)]
-   Test suite: Memory leak in vm.runInNewContext() seems fixed. Revert [[`9a4b6fa`](https://github.com/PrismJS/prism/commit/9a4b6fa)] to drastically improve tests execution time. [[`9bceece`](https://github.com/PrismJS/prism/commit/9bceece), [`7c7602b`](https://github.com/PrismJS/prism/commit/7c7602b)]
-   Gulp: Don't minify `components/index.js` [[`689227b`](https://github.com/PrismJS/prism/commit/689227b)]
-   Website: Fix theme selection on Download page, when theme is in query string or hash. [[`b4d3063`](https://github.com/PrismJS/prism/commit/b4d3063)]
-   Update JSPM config to also include unminified components. Close [#995](https://github.com/PrismJS/prism/issues/995) [[`218f160`](https://github.com/PrismJS/prism/commit/218f160)]
-   Core: Fix support for language alias containing dash `-` [[`659ea31`](https://github.com/PrismJS/prism/commit/659ea31)]

## 1.13.0 (2018-03-21)

### New components

-   **ERB** [[`e6213ac`](https://github.com/PrismJS/prism/commit/e6213ac)]
-   **PL/SQL** ([#1338](https://github.com/PrismJS/prism/issues/1338)) [[`3599e6a`](https://github.com/PrismJS/prism/commit/3599e6a)]

### Updated components

-   **JSX**: \* Add support for plain text inside tags ([#1357](https://github.com/PrismJS/prism/issues/1357)) [[`2b8321d`](https://github.com/PrismJS/prism/commit/2b8321d)]
-   **Markup**: \* Make tags greedy. Fix [#1356](https://github.com/PrismJS/prism/issues/1356) [[`af834be`](https://github.com/PrismJS/prism/commit/af834be)]
-   **Powershell**: \* Add lookbehind to fix function interpolation inside strings. Fix [#1361](https://github.com/PrismJS/prism/issues/1361) [[`d2c026e`](https://github.com/PrismJS/prism/commit/d2c026e)]
-   **Rust**: \* Improve char pattern so that lifetime annotations are matched better. Fix [#1353](https://github.com/PrismJS/prism/issues/1353) [[`efdccbf`](https://github.com/PrismJS/prism/commit/efdccbf)]

### Updated themes

-   **Default**: \* Add color for class names [[`8572474`](https://github.com/PrismJS/prism/commit/8572474)]
-   **Coy**: \* Inherit pre's height on code, so it does not break on Download page. [[`c6c7fd1`](https://github.com/PrismJS/prism/commit/c6c7fd1)]

### Other changes

-   Website: Auto-generate example headers [[`c3ed5b5`](https://github.com/PrismJS/prism/commit/c3ed5b5)]
-   Core: Allow cloning of circular structures. ([#1345](https://github.com/PrismJS/prism/issues/1345)) [[`f90d555`](https://github.com/PrismJS/prism/commit/f90d555)]
-   Core: Generate components.js from components.json and make it exportable to nodeJS. ([#1354](https://github.com/PrismJS/prism/issues/1354)) [[`ba60df0`](https://github.com/PrismJS/prism/commit/ba60df0)]
-   Website: Improve appearance of theme selector [[`0460cad`](https://github.com/PrismJS/prism/commit/0460cad)]
-   Website: Check stored theme by default + link both theme selectors together. Close [#1038](https://github.com/PrismJS/prism/issues/1038) [[`212dd4e`](https://github.com/PrismJS/prism/commit/212dd4e)]
-   Tests: Use the new components.js file directly [[`0e1a8b7`](https://github.com/PrismJS/prism/commit/0e1a8b7)]
-   Update .npmignore Close [#1274](https://github.com/PrismJS/prism/issues/1274) [[`a52319a`](https://github.com/PrismJS/prism/commit/a52319a)]
-   Add a loadLanguages() function for easy component loading on NodeJS ([#1359](https://github.com/PrismJS/prism/issues/1359)) [[`a5331a6`](https://github.com/PrismJS/prism/commit/a5331a6)]

## 1.12.2 (2018-03-08)

### Other changes

-   Test against NodeJS 4, 6, 8 and 9 ([#1329](https://github.com/PrismJS/prism/issues/1329)) [[`97b7d0a`](https://github.com/PrismJS/prism/commit/97b7d0a)]
-   Stop testing against NodeJS 0.10 and 0.12 [[`df01b1b`](https://github.com/PrismJS/prism/commit/df01b1b)]

## 1.12.1 (2018-03-08)

### Updated components

-   **C-like**: \* Revert [[`b98e5b9`](https://github.com/PrismJS/prism/commit/b98e5b9)] to fix [#1340](https://github.com/PrismJS/prism/issues/1340). Reopened [#1337](https://github.com/PrismJS/prism/issues/1337). [[`cebacdf`](https://github.com/PrismJS/prism/commit/cebacdf)]
-   **JSX**: \* Allow for one level of nested curly braces inside tag attribute value. Fix [#1335](https://github.com/PrismJS/prism/issues/1335) [[`05bf67d`](https://github.com/PrismJS/prism/commit/05bf67d)]
-   **Ruby**: \* Ensure module syntax is not confused with symbols. Fix [#1336](https://github.com/PrismJS/prism/issues/1336) [[`31a2a69`](https://github.com/PrismJS/prism/commit/31a2a69)]

## 1.12.0 (2018-03-07)

### New components

-   **ARFF** ([#1327](https://github.com/PrismJS/prism/issues/1327)) [[`0bc98ac`](https://github.com/PrismJS/prism/commit/0bc98ac)]
-   **Clojure** ([#1311](https://github.com/PrismJS/prism/issues/1311)) [[`8b4d3bd`](https://github.com/PrismJS/prism/commit/8b4d3bd)]
-   **Liquid** ([#1326](https://github.com/PrismJS/prism/issues/1326)) [[`f0b2c9e`](https://github.com/PrismJS/prism/commit/f0b2c9e)]

### Updated components

-   **Bash**:
    _ Add shell as an alias ([#1321](https://github.com/PrismJS/prism/issues/1321)) [[`67e16a2`](https://github.com/PrismJS/prism/commit/67e16a2)]
    _ Add support for quoted command substitution. Fix [#1287](https://github.com/PrismJS/prism/issues/1287) [[`63fc215`](https://github.com/PrismJS/prism/commit/63fc215)]
-   **C#**: \* Add "dotnet" alias. [[`405867c`](https://github.com/PrismJS/prism/commit/405867c)]
-   **C-like**: \* Change order of comment patterns and make multi-line one greedy. Fix [#1337](https://github.com/PrismJS/prism/issues/1337) [[`b98e5b9`](https://github.com/PrismJS/prism/commit/b98e5b9)]
-   **NSIS**:
    _ Add support for NSIS 3.03 ([#1288](https://github.com/PrismJS/prism/issues/1288)) [[`bd1e98b`](https://github.com/PrismJS/prism/commit/bd1e98b)]
    _ Add missing NSIS commands ([#1289](https://github.com/PrismJS/prism/issues/1289)) [[`ad2948f`](https://github.com/PrismJS/prism/commit/ad2948f)]
-   **PHP**:
    _ Add support for string interpolation inside double-quoted strings. Fix [#1146](https://github.com/PrismJS/prism/issues/1146) [[`9f1f8d6`](https://github.com/PrismJS/prism/commit/9f1f8d6)]
    _ Add support for Heredoc and Nowdoc strings [[`5d7223c`](https://github.com/PrismJS/prism/commit/5d7223c)] \* Fix shell-comment failure now that strings are greedy [[`ad25d22`](https://github.com/PrismJS/prism/commit/ad25d22)]
-   **PowerShell**: \* Add support for two levels of nested brackets inside namespace pattern. Fixes [#1317](https://github.com/PrismJS/prism/issues/1317) [[`3bc3e9c`](https://github.com/PrismJS/prism/commit/3bc3e9c)]
-   **Ruby**: \* Add keywords "protected", "private" and "public" [[`4593837`](https://github.com/PrismJS/prism/commit/4593837)]
-   **Rust**: \* Add support for lifetime-annotation and => operator. Fix [#1339](https://github.com/PrismJS/prism/issues/1339) [[`926f6f8`](https://github.com/PrismJS/prism/commit/926f6f8)]
-   **Scheme**: \* Don't highlight first number of a list as a function. Fix [#1331](https://github.com/PrismJS/prism/issues/1331) [[`51bff80`](https://github.com/PrismJS/prism/commit/51bff80)]
-   **SQL**: \* Add missing keywords and functions, fix numbers [[`de29d4a`](https://github.com/PrismJS/prism/commit/de29d4a)]

### Updated plugins

-   **Autolinker**: \* Allow more chars in query string and hash to match more URLs. Fix [#1142](https://github.com/PrismJS/prism/issues/1142) [[`109bd6f`](https://github.com/PrismJS/prism/commit/109bd6f)]
-   **Copy to Clipboard**: \* Bump ClipboardJS to 2.0.0 and remove hack ([#1314](https://github.com/PrismJS/prism/issues/1314)) [[`e9f410e`](https://github.com/PrismJS/prism/commit/e9f410e)]
-   **Toolbar**: \* Prevent scrolling toolbar with content ([#1305](https://github.com/PrismJS/prism/issues/1305), [#1314](https://github.com/PrismJS/prism/issues/1314)) [[`84eeb89`](https://github.com/PrismJS/prism/commit/84eeb89)]
-   **Unescaped Markup**: \* Use msMatchesSelector for IE11 and below. Fix [#1302](https://github.com/PrismJS/prism/issues/1302) [[`c246c1a`](https://github.com/PrismJS/prism/commit/c246c1a)]
-   **WebPlatform Docs**: \* WebPlatform Docs plugin: Fix links. Fixes [#1290](https://github.com/PrismJS/prism/issues/1290) [[`7a9dbe0`](https://github.com/PrismJS/prism/commit/7a9dbe0)]

### Other changes

-   Fix Autoloader's demo page [[`3dddac9`](https://github.com/PrismJS/prism/commit/3dddac9)]
-   Download page: Use hash instead of query-string for redownload URL. Fix [#1263](https://github.com/PrismJS/prism/issues/1263) [[`b03c02a`](https://github.com/PrismJS/prism/commit/b03c02a)]
-   Core: Don't thow an error if lookbehing is used without anything matching. [[`e0cd47f`](https://github.com/PrismJS/prism/commit/e0cd47f)]
-   Docs: Fix link to the `<code>` element specification in HTML5 [[`a84263f`](https://github.com/PrismJS/prism/commit/a84263f)]
-   Docs: Mention support for `lang-xxxx` class. Close [#1312](https://github.com/PrismJS/prism/issues/1312) [[`a9e76db`](https://github.com/PrismJS/prism/commit/a9e76db)]
-   Docs: Add note on `async` parameter to clarify the requirement of using a single bundled file. Closes [#1249](https://github.com/PrismJS/prism/issues/1249) [[`eba0235`](https://github.com/PrismJS/prism/commit/eba0235)]

## 1.11.0 (2018-02-05)

### New components

-   **Content-Security-Policy (CSP)** ([#1275](https://github.com/PrismJS/prism/issues/1275)) [[`b08cae5`](https://github.com/PrismJS/prism/commit/b08cae5)]
-   **HTTP Public-Key-Pins (HPKP)** ([#1275](https://github.com/PrismJS/prism/issues/1275)) [[`b08cae5`](https://github.com/PrismJS/prism/commit/b08cae5)]
-   **HTTP String-Transport-Security (HSTS)** ([#1275](https://github.com/PrismJS/prism/issues/1275)) [[`b08cae5`](https://github.com/PrismJS/prism/commit/b08cae5)]
-   **React TSX** ([#1280](https://github.com/PrismJS/prism/issues/1280)) [[`fbe82b8`](https://github.com/PrismJS/prism/commit/fbe82b8)]

### Updated components

-   **C++**: \* Add C++ platform-independent types ([#1271](https://github.com/PrismJS/prism/issues/1271)) [[`3da238f`](https://github.com/PrismJS/prism/commit/3da238f)]
-   **TypeScript**: \* Improve typescript with builtins ([#1277](https://github.com/PrismJS/prism/issues/1277)) [[`5de1b1f`](https://github.com/PrismJS/prism/commit/5de1b1f)]

### Other changes

-   Fix passing of non-enumerable Error properties from the child test runner ([#1276](https://github.com/PrismJS/prism/issues/1276)) [[`38df653`](https://github.com/PrismJS/prism/commit/38df653)]

## 1.10.0 (2018-01-17)

### New components

-   **6502 Assembly** ([#1245](https://github.com/PrismJS/prism/issues/1245)) [[`2ece18b`](https://github.com/PrismJS/prism/commit/2ece18b)]
-   **Elm** ([#1174](https://github.com/PrismJS/prism/issues/1174)) [[`d6da70e`](https://github.com/PrismJS/prism/commit/d6da70e)]
-   **IchigoJam BASIC** ([#1246](https://github.com/PrismJS/prism/issues/1246)) [[`cf840be`](https://github.com/PrismJS/prism/commit/cf840be)]
-   **Io** ([#1251](https://github.com/PrismJS/prism/issues/1251)) [[`84ed3ed`](https://github.com/PrismJS/prism/commit/84ed3ed)]

### Updated components

-   **BASIC**: \* Make strings greedy [[`60114d0`](https://github.com/PrismJS/prism/commit/60114d0)]
-   **C++**: \* Add C++11 raw string feature ([#1254](https://github.com/PrismJS/prism/issues/1254)) [[`71595be`](https://github.com/PrismJS/prism/commit/71595be)]

### Updated plugins

-   **Autoloader**: \* Add support for `data-autoloader-path` ([#1242](https://github.com/PrismJS/prism/issues/1242)) [[`39360d6`](https://github.com/PrismJS/prism/commit/39360d6)]
-   **Previewers**: \* New plugin combining previous plugins Previewer: Base, Previewer: Angle, Previewer: Color, Previewer: Easing, Previewer: Gradient and Previewer: Time. ([#1244](https://github.com/PrismJS/prism/issues/1244)) [[`28e4b4c`](https://github.com/PrismJS/prism/commit/28e4b4c)]
-   **Unescaped Markup**: \* Make it work with any language ([#1265](https://github.com/PrismJS/prism/issues/1265)) [[`7bcdae7`](https://github.com/PrismJS/prism/commit/7bcdae7)]

### Other changes

-   Add attribute `style` in `package.json` ([#1256](https://github.com/PrismJS/prism/issues/1256)) [[`a9b6785`](https://github.com/PrismJS/prism/commit/a9b6785)]

## 1.9.0 (2017-12-06)

### New components

-   **Flow** [[`d27b70d`](https://github.com/PrismJS/prism/commit/d27b70d)]

### Updated components

-   **CSS**: \* Unicode characters in CSS properties ([#1227](https://github.com/PrismJS/prism/issues/1227)) [[`f234ea4`](https://github.com/PrismJS/prism/commit/f234ea4)]
-   **JSX**: \* JSX: Improve highlighting support. Fix [#1235](https://github.com/PrismJS/prism/issues/1235) and [#1236](https://github.com/PrismJS/prism/issues/1236) [[`f41c5cd`](https://github.com/PrismJS/prism/commit/f41c5cd)]
-   **Markup**: \* Make CSS and JS inclusions in Markup greedy. Fix [#1240](https://github.com/PrismJS/prism/issues/1240) [[`7dc1e45`](https://github.com/PrismJS/prism/commit/7dc1e45)]
-   **PHP**: \* Add support for multi-line strings. Fix [#1233](https://github.com/PrismJS/prism/issues/1233) [[`9a542a0`](https://github.com/PrismJS/prism/commit/9a542a0)]

### Updated plugins

-   **Copy to clipboard**:
    _ Fix test for native Clipboard. Fix [#1241](https://github.com/PrismJS/prism/issues/1241) [[`e7b5e82`](https://github.com/PrismJS/prism/commit/e7b5e82)]
    _ Copy to clipboard: Update to v1.7.1. Fix [#1220](https://github.com/PrismJS/prism/issues/1220) [[`a1b85e3`](https://github.com/PrismJS/prism/commit/a1b85e3), [`af50e44`](https://github.com/PrismJS/prism/commit/af50e44)]
-   **Line highlight**: \* Fixes to compatibility of line number and line higlight plugins ([#1194](https://github.com/PrismJS/prism/issues/1194)) [[`e63058f`](https://github.com/PrismJS/prism/commit/e63058f), [`3842a91`](https://github.com/PrismJS/prism/commit/3842a91)]
-   **Unescaped Markup**: \* Fix ambiguity in documentation by improving examples. Fix [#1197](https://github.com/PrismJS/prism/issues/1197) [[`924784a`](https://github.com/PrismJS/prism/commit/924784a)]

### Other changes

-   Allow any element being root instead of document. ([#1230](https://github.com/PrismJS/prism/issues/1230)) [[`69f2e2c`](https://github.com/PrismJS/prism/commit/69f2e2c), [`6e50d44`](https://github.com/PrismJS/prism/commit/6e50d44)]
-   Coy Theme: The 'height' element makes code blocks the height of the browser canvas. ([#1224](https://github.com/PrismJS/prism/issues/1224)) [[`ac219d7`](https://github.com/PrismJS/prism/commit/ac219d7)]
-   Download page: Fix implicitly declared variable [[`f986551`](https://github.com/PrismJS/prism/commit/f986551)]
-   Download page: Add version number at the beginning of the generated files. Fix [#788](https://github.com/PrismJS/prism/issues/788) [[`928790d`](https://github.com/PrismJS/prism/commit/928790d)]

## 1.8.4 (2017-11-05)

### Updated components

-   **ABAP**: \* Regexp optimisation [[`7547f83`](https://github.com/PrismJS/prism/commit/7547f83)]
-   **ActionScript**: \* Fix XML regex + optimise [[`75d00d7`](https://github.com/PrismJS/prism/commit/75d00d7)]
-   **Ada**: \* Regexp simplification [[`e881fe3`](https://github.com/PrismJS/prism/commit/e881fe3)]
-   **Apacheconf**: \* Regexp optimisation [[`a065e61`](https://github.com/PrismJS/prism/commit/a065e61)]
-   **APL**: \* Regexp simplification [[`33297c4`](https://github.com/PrismJS/prism/commit/33297c4)]
-   **AppleScript**: \* Regexp optimisation [[`d879f36`](https://github.com/PrismJS/prism/commit/d879f36)]
-   **Arduino**: \* Don't use captures if not needed [[`16b338f`](https://github.com/PrismJS/prism/commit/16b338f)]
-   **ASP.NET**: \* Regexp optimisation [[`438926c`](https://github.com/PrismJS/prism/commit/438926c)]
-   **AutoHotkey**: \* Regexp simplification + don't use captures if not needed [[`5edfd2f`](https://github.com/PrismJS/prism/commit/5edfd2f)]
-   **Bash**: \* Regexp optimisation and simplification [[`75b9b29`](https://github.com/PrismJS/prism/commit/75b9b29)]
-   **Bro**: \* Regexp simplification + don't use captures if not needed [[`d4b9003`](https://github.com/PrismJS/prism/commit/d4b9003)]
-   **C**: \* Regexp optimisation + don't use captures if not needed [[`f61d487`](https://github.com/PrismJS/prism/commit/f61d487)]
-   **C++**: \* Fix operator regexp + regexp simplification + don't use captures if not needed [[`ffeb26e`](https://github.com/PrismJS/prism/commit/ffeb26e)]
-   **C#**: \* Remove duplicates in keywords + regexp optimisation + don't use captures if not needed [[`d28d178`](https://github.com/PrismJS/prism/commit/d28d178)]
-   **C-like**: \* Regexp simplification + don't use captures if not needed [[`918e0ff`](https://github.com/PrismJS/prism/commit/918e0ff)]
-   **CoffeeScript**: \* Regexp optimisation + don't use captures if not needed [[`5895978`](https://github.com/PrismJS/prism/commit/5895978)]
-   **Crystal**: \* Remove trailing comma [[`16979a3`](https://github.com/PrismJS/prism/commit/16979a3)]
-   **CSS**: \* Regexp simplification + don't use captures if not needed + handle multi-line style attributes [[`43d9f36`](https://github.com/PrismJS/prism/commit/43d9f36)]
-   **CSS Extras**: \* Regexp simplification [[`134ed70`](https://github.com/PrismJS/prism/commit/134ed70)]
-   **D**: \* Regexp optimisation [[`fbe39c9`](https://github.com/PrismJS/prism/commit/fbe39c9)]
-   **Dart**: \* Regexp optimisation [[`f24e919`](https://github.com/PrismJS/prism/commit/f24e919)]
-   **Django**: \* Regexp optimisation [[`a95c51d`](https://github.com/PrismJS/prism/commit/a95c51d)]
-   **Docker**: \* Regexp optimisation [[`27f99ff`](https://github.com/PrismJS/prism/commit/27f99ff)]
-   **Eiffel**: \* Regexp optimisation [[`b7cdea2`](https://github.com/PrismJS/prism/commit/b7cdea2)]
-   **Elixir**: \* Regexp optimisation + uniform behavior between ~r and ~s [[`5d12e80`](https://github.com/PrismJS/prism/commit/5d12e80)]
-   **Erlang**: \* Regexp optimisation [[`e7b411e`](https://github.com/PrismJS/prism/commit/e7b411e)]
-   **F#**: \* Regexp optimisation + don't use captures if not needed [[`7753fc4`](https://github.com/PrismJS/prism/commit/7753fc4)]
-   **Gherkin**: \* Regexp optimisation + don't use captures if not needed + added explanation comment on table-body regexp [[`f26197a`](https://github.com/PrismJS/prism/commit/f26197a)]
-   **Git**: \* Regexp optimisation [[`b9483b9`](https://github.com/PrismJS/prism/commit/b9483b9)]
-   **GLSL**: \* Regexp optimisation [[`e66d21b`](https://github.com/PrismJS/prism/commit/e66d21b)]
-   **Go**: \* Regexp optimisation + don't use captures if not needed [[`88caabb`](https://github.com/PrismJS/prism/commit/88caabb)]
-   **GraphQL**: \* Regexp optimisation and simplification [[`2474f06`](https://github.com/PrismJS/prism/commit/2474f06)]
-   **Groovy**: \* Regexp optimisation + don't use captures if not needed [[`e74e00c`](https://github.com/PrismJS/prism/commit/e74e00c)]
-   **Haml**: \* Regexp optimisation + don't use captures if not needed + fix typo in comment [[`23e3b43`](https://github.com/PrismJS/prism/commit/23e3b43)]
-   **Handlebars**: \* Regexp optimisation + don't use captures if not needed [[`09dbfce`](https://github.com/PrismJS/prism/commit/09dbfce)]
-   **Haskell**: \* Regexp simplification + don't use captures if not needed [[`f11390a`](https://github.com/PrismJS/prism/commit/f11390a)]
-   **HTTP**: \* Regexp simplification + don't use captures if not needed [[`37ef24e`](https://github.com/PrismJS/prism/commit/37ef24e)]
-   **Icon**: \* Regexp optimisation [[`9cf64a0`](https://github.com/PrismJS/prism/commit/9cf64a0)]
-   **J**: \* Regexp simplification [[`de15150`](https://github.com/PrismJS/prism/commit/de15150)]
-   **Java**: \* Don't use captures if not needed [[`96b35c8`](https://github.com/PrismJS/prism/commit/96b35c8)]
-   **JavaScript**: \* Regexp optimisation + don't use captures if not needed [[`93d4002`](https://github.com/PrismJS/prism/commit/93d4002)]
-   **Jolie**: \* Regexp optimisation + don't use captures if not needed + remove duplicates in keywords [[`a491f9e`](https://github.com/PrismJS/prism/commit/a491f9e)]
-   **JSON**:
    _ Make strings greedy, remove negative look-ahead for ":". Fix [#1204](https://github.com/PrismJS/prism/issues/1204) [[`98acd2d`](https://github.com/PrismJS/prism/commit/98acd2d)]
    _ Regexp optimisation + don't use captures if not needed [[`8fc1b03`](https://github.com/PrismJS/prism/commit/8fc1b03)]
-   **JSX**: \* Regexp optimisation + handle spread operator as a whole [[`28de4e2`](https://github.com/PrismJS/prism/commit/28de4e2)]
-   **Julia**: \* Regexp optimisation and simplification [[`12684c0`](https://github.com/PrismJS/prism/commit/12684c0)]
-   **Keyman**: \* Regexp optimisation + don't use captures if not needed [[`9726087`](https://github.com/PrismJS/prism/commit/9726087)]
-   **Kotlin**: \* Regexp simplification [[`12ff8dc`](https://github.com/PrismJS/prism/commit/12ff8dc)]
-   **LaTeX**: \* Regexp optimisation and simplification [[`aa426b0`](https://github.com/PrismJS/prism/commit/aa426b0)]
-   **LiveScript**: \* Make interpolated strings greedy + fix variable and identifier regexps [[`c581049`](https://github.com/PrismJS/prism/commit/c581049)]
-   **LOLCODE**: \* Don't use captures if not needed [[`52903af`](https://github.com/PrismJS/prism/commit/52903af)]
-   **Makefile**: \* Regexp optimisation [[`20ae2e5`](https://github.com/PrismJS/prism/commit/20ae2e5)]
-   **Markdown**: \* Don't use captures if not needed [[`f489a1e`](https://github.com/PrismJS/prism/commit/f489a1e)]
-   **Markup**: \* Regexp optimisation + fix punctuation inside attr-value [[`ea380c6`](https://github.com/PrismJS/prism/commit/ea380c6)]
-   **MATLAB**: \* Make strings greedy + handle line feeds better [[`4cd4f01`](https://github.com/PrismJS/prism/commit/4cd4f01)]
-   **Monkey**: \* Don't use captures if not needed [[`7f47140`](https://github.com/PrismJS/prism/commit/7f47140)]
-   **N4JS**: \* Don't use captures if not needed [[`2d3f9df`](https://github.com/PrismJS/prism/commit/2d3f9df)]
-   **NASM**: \* Regexp optimisation and simplification + don't use captures if not needed [[`9937428`](https://github.com/PrismJS/prism/commit/9937428)]
-   **nginx**: \* Remove trailing comma + remove duplicates in keywords [[`c6e7195`](https://github.com/PrismJS/prism/commit/c6e7195)]
-   **NSIS**: \* Regexp optimisation + don't use captures if not needed [[`beeb107`](https://github.com/PrismJS/prism/commit/beeb107)]
-   **Objective-C**: \* Don't use captures if not needed [[`9be0f88`](https://github.com/PrismJS/prism/commit/9be0f88)]
-   **OCaml**: \* Regexp simplification [[`5f5f38c`](https://github.com/PrismJS/prism/commit/5f5f38c)]
-   **OpenCL**: \* Don't use captures if not needed [[`5e70f1d`](https://github.com/PrismJS/prism/commit/5e70f1d)]
-   **Oz**: \* Fix atom regexp [[`9320e92`](https://github.com/PrismJS/prism/commit/9320e92)]
-   **PARI/GP**: \* Regexp optimisation [[`2c7b59b`](https://github.com/PrismJS/prism/commit/2c7b59b)]
-   **Parser**: \* Regexp simplification [[`569d511`](https://github.com/PrismJS/prism/commit/569d511)]
-   **Perl**: \* Regexp optimisation and simplification + don't use captures if not needed [[`0fe4cf6`](https://github.com/PrismJS/prism/commit/0fe4cf6)]
-   **PHP**: \* Don't use captures if not needed Golmote [[`5235f18`](https://github.com/PrismJS/prism/commit/5235f18)]
-   **PHP Extras**: \* Add word boundary after global keywords + don't use captures if not needed [[`9049a2a`](https://github.com/PrismJS/prism/commit/9049a2a)]
-   **PowerShell**: \* Regexp optimisation + don't use captures if not needed [[`0d05957`](https://github.com/PrismJS/prism/commit/0d05957)]
-   **Processing**: \* Regexp simplification [[`8110d38`](https://github.com/PrismJS/prism/commit/8110d38)]
-   **.properties**: \* Regexp optimisation [[`678b621`](https://github.com/PrismJS/prism/commit/678b621)]
-   **Protocol Buffers**: \* Don't use captures if not needed [[`3e256d8`](https://github.com/PrismJS/prism/commit/3e256d8)]
-   **Pug**: \* Don't use captures if not needed [[`76dc925`](https://github.com/PrismJS/prism/commit/76dc925)]
-   **Pure**: \* Make inline-lang greedy [[`92318b0`](https://github.com/PrismJS/prism/commit/92318b0)]
-   **Python**:
    _ Add Python builtin function highlighting ([#1205](https://github.com/PrismJS/prism/issues/1205)) [[`2169c99`](https://github.com/PrismJS/prism/commit/2169c99)]
    _ Python: Add highlighting to functions with space between name and parentheses ([#1207](https://github.com/PrismJS/prism/issues/1207)) [[`3badd8a`](https://github.com/PrismJS/prism/commit/3badd8a)] \* Make triple-quoted strings greedy + regexp optimisation and simplification [[`f09f9f5`](https://github.com/PrismJS/prism/commit/f09f9f5)]
-   **Qore**: \* Regexp simplification [[`69459f0`](https://github.com/PrismJS/prism/commit/69459f0)]
-   **R**: \* Regexp optimisation [[`06a9da4`](https://github.com/PrismJS/prism/commit/06a9da4)]
-   **Reason**: \* Regexp optimisation + don't use capture if not needed [[`19d79b4`](https://github.com/PrismJS/prism/commit/19d79b4)]
-   **Ren'py**: \* Make strings greedy + don't use captures if not needed [[`91d84d9`](https://github.com/PrismJS/prism/commit/91d84d9)]
-   **reST**: \* Regexp simplification + don't use captures if not needed [[`1a8b3e9`](https://github.com/PrismJS/prism/commit/1a8b3e9)]
-   **Rip**: \* Regexp optimisation [[`d7f0ee8`](https://github.com/PrismJS/prism/commit/d7f0ee8)]
-   **Ruby**: \* Regexp optimisation and simplification + don't use captures if not needed [[`4902ed4`](https://github.com/PrismJS/prism/commit/4902ed4)]
-   **Rust**: \* Regexp optimisation and simplification + don't use captures if not needed [[`cc9d874`](https://github.com/PrismJS/prism/commit/cc9d874)]
-   **Sass**: \* Regexp simplification Golmote [[`165d957`](https://github.com/PrismJS/prism/commit/165d957)]
-   **Scala**: \* Regexp optimisation Golmote [[`5f50c12`](https://github.com/PrismJS/prism/commit/5f50c12)]
-   **Scheme**: \* Regexp optimisation [[`bd19b04`](https://github.com/PrismJS/prism/commit/bd19b04)]
-   **SCSS**: \* Regexp simplification [[`c60b7d4`](https://github.com/PrismJS/prism/commit/c60b7d4)]
-   **Smalltalk**: \* Regexp simplification [[`41a2c76`](https://github.com/PrismJS/prism/commit/41a2c76)]
-   **Smarty**: \* Regexp optimisation and simplification [[`e169be9`](https://github.com/PrismJS/prism/commit/e169be9)]
-   **SQL**: \* Regexp optimisation [[`a6244a4`](https://github.com/PrismJS/prism/commit/a6244a4)]
-   **Stylus**: \* Regexp optimisation [[`df9506c`](https://github.com/PrismJS/prism/commit/df9506c)]
-   **Swift**: \* Don't use captures if not needed [[`a2d737a`](https://github.com/PrismJS/prism/commit/a2d737a)]
-   **Tcl**: \* Regexp simplification + don't use captures if not needed [[`f0b8a33`](https://github.com/PrismJS/prism/commit/f0b8a33)]
-   **Textile**: \* Regexp optimisation + don't use captures if not needed [[`08139ad`](https://github.com/PrismJS/prism/commit/08139ad)]
-   **Twig**: \* Regexp optimisation and simplification + don't use captures if not needed [[`0b10fd0`](https://github.com/PrismJS/prism/commit/0b10fd0)]
-   **TypeScript**: \* Don't use captures if not needed [[`e296caf`](https://github.com/PrismJS/prism/commit/e296caf)]
-   **Verilog**: \* Regexp simplification [[`1b24b34`](https://github.com/PrismJS/prism/commit/1b24b34)]
-   **VHDL**: \* Regexp optimisation and simplification [[`7af36df`](https://github.com/PrismJS/prism/commit/7af36df)]
-   **vim**: \* Remove duplicates in keywords [[`700505e`](https://github.com/PrismJS/prism/commit/700505e)]
-   **Wiki markup**: \* Fix escaping consistency [[`1fd690d`](https://github.com/PrismJS/prism/commit/1fd690d)]
-   **YAML**: \* Regexp optimisation + don't use captures if not needed [[`1fd690d`](https://github.com/PrismJS/prism/commit/1fd690d)]

### Other changes

-   Remove comments spellcheck for AMP validation ([#1106](https://github.com/PrismJS/prism/issues/1106)) [[`de996d7`](https://github.com/PrismJS/prism/commit/de996d7)]
-   Prevent error from throwing when element does not have a parentNode in highlightElement. [[`c33be19`](https://github.com/PrismJS/prism/commit/c33be19)]
-   Provide a way to load Prism from inside a Worker without listening to messages. ([#1188](https://github.com/PrismJS/prism/issues/1188)) [[`d09982d`](https://github.com/PrismJS/prism/commit/d09982d)]

## 1.8.3 (2017-10-19)

### Other changes

-   Fix inclusion tests for Pug [[`955c2ab`](https://github.com/PrismJS/prism/commit/955c2ab)]

## 1.8.2 (2017-10-19)

### Updated components

-   **Jade**: \* Jade has been renamed to **Pug** ([#1201](https://github.com/PrismJS/prism/issues/1201)) [[`bcfef7c`](https://github.com/PrismJS/prism/commit/bcfef7c)]
-   **JavaScript**: \* Better highlighting of functions ([#1190](https://github.com/PrismJS/prism/issues/1190)) [[`8ee2cd3`](https://github.com/PrismJS/prism/commit/8ee2cd3)]

### Update plugins

-   **Copy to clipboard**: \* Fix error occurring when using in Chrome 61+ ([#1206](https://github.com/PrismJS/prism/issues/1206)) [[`b41d571`](https://github.com/PrismJS/prism/commit/b41d571)]
-   **Show invisibles**: \* Prevent error when using with Autoloader plugin ([#1195](https://github.com/PrismJS/prism/issues/1195)) [[`ed8bdb5`](https://github.com/PrismJS/prism/commit/ed8bdb5)]

## 1.8.1 (2017-09-16)

### Other changes

-   Add Arduino to components.js [[`290a3c6`](https://github.com/PrismJS/prism/commit/290a3c6)]

## 1.8.0 (2017-09-16)

### New components

-   **Arduino** ([#1184](https://github.com/PrismJS/prism/issues/1184)) [[`edf2454`](https://github.com/PrismJS/prism/commit/edf2454)]
-   **OpenCL** ([#1175](https://github.com/PrismJS/prism/issues/1175)) [[`131e8fa`](https://github.com/PrismJS/prism/commit/131e8fa)]

### Updated plugins

-   **Autolinker**: \* Silently catch any error thrown by decodeURIComponent. Fixes [#1186](https://github.com/PrismJS/prism/issues/1186) [[`2e43fcf`](https://github.com/PrismJS/prism/commit/2e43fcf)]

## 1.7.0 (2017-09-09)

### New components

-   **Django/Jinja2** ([#1085](https://github.com/PrismJS/prism/issues/1085)) [[`345b1b2`](https://github.com/PrismJS/prism/commit/345b1b2)]
-   **N4JS** ([#1141](https://github.com/PrismJS/prism/issues/1141)) [[`eaa8ebb`](https://github.com/PrismJS/prism/commit/eaa8ebb)]
-   **Ren'py** ([#658](https://github.com/PrismJS/prism/issues/658)) [[`7ab4013`](https://github.com/PrismJS/prism/commit/7ab4013)]
-   **VB.Net** ([#1122](https://github.com/PrismJS/prism/issues/1122)) [[`5400651`](https://github.com/PrismJS/prism/commit/5400651)]

### Updated components

-   **APL**:
    _ Add left shoe underbar and right shoe underbar ([#1072](https://github.com/PrismJS/prism/issues/1072)) [[`12238c5`](https://github.com/PrismJS/prism/commit/12238c5)]
    _ Update prism-apl.js ([#1126](https://github.com/PrismJS/prism/issues/1126)) [[`a5f3cdb`](https://github.com/PrismJS/prism/commit/a5f3cdb)]
-   **C**: \* Add more keywords and constants for C. ([#1029](https://github.com/PrismJS/prism/issues/1029)) [[`43a388e`](https://github.com/PrismJS/prism/commit/43a388e)]
-   **C#**: \* Fix wrong highlighting when three slashes appear inside string. Fix [#1091](https://github.com/PrismJS/prism/issues/1091) [[`dfb6f17`](https://github.com/PrismJS/prism/commit/dfb6f17)]
-   **C-like**: \* Add support for unclosed block comments. Close [#828](https://github.com/PrismJS/prism/issues/828) [[`3426ed1`](https://github.com/PrismJS/prism/commit/3426ed1)]
-   **Crystal**: \* Update Crystal keywords ([#1092](https://github.com/PrismJS/prism/issues/1092)) [[`125bff1`](https://github.com/PrismJS/prism/commit/125bff1)]
-   **CSS Extras**: \* Support CSS #RRGGBBAA ([#1139](https://github.com/PrismJS/prism/issues/1139)) [[`07a6806`](https://github.com/PrismJS/prism/commit/07a6806)]
-   **Docker**:
    _ Add dockerfile alias for docker language ([#1164](https://github.com/PrismJS/prism/issues/1164)) [[`601c47f`](https://github.com/PrismJS/prism/commit/601c47f)]
    _ Update the list of keywords for dockerfiles ([#1180](https://github.com/PrismJS/prism/issues/1180)) [[`f0d73e0`](https://github.com/PrismJS/prism/commit/f0d73e0)]
-   **Eiffel**: \* Add class-name highlighting for Eiffel ([#471](https://github.com/PrismJS/prism/issues/471)) [[`cd03587`](https://github.com/PrismJS/prism/commit/cd03587)]
-   **Handlebars**: \* Check for possible pre-existing marker strings in Handlebars [[`7a1a404`](https://github.com/PrismJS/prism/commit/7a1a404)]
-   **JavaScript**:
    _ Properly match every operator as a whole token. Fix [#1133](https://github.com/PrismJS/prism/issues/1133) [[`9f649fb`](https://github.com/PrismJS/prism/commit/9f649fb)]
    _ Allows uppercase prefixes in JS number literals ([#1151](https://github.com/PrismJS/prism/issues/1151)) [[`d4ee904`](https://github.com/PrismJS/prism/commit/d4ee904)] \* Reduced backtracking in regex pattern. Fix [#1159](https://github.com/PrismJS/prism/issues/1159) [[`ac09e97`](https://github.com/PrismJS/prism/commit/ac09e97)]
-   **JSON**: \* Fix property and string patterns performance. Fix [#1080](https://github.com/PrismJS/prism/issues/1080) [[`0ca1353`](https://github.com/PrismJS/prism/commit/0ca1353)]
-   **JSX**:
    _ JSX spread operator break. Fixes [#1061](https://github.com/PrismJS/prism/issues/1061) ([#1094](https://github.com/PrismJS/prism/issues/1094)) [[`561bceb`](https://github.com/PrismJS/prism/commit/561bceb)]
    _ Fix highlighting of attributes containing spaces [[`867ea42`](https://github.com/PrismJS/prism/commit/867ea42)] \* Improved performance for tags (when not matching) Fix [#1152](https://github.com/PrismJS/prism/issues/1152) [[`b0fe103`](https://github.com/PrismJS/prism/commit/b0fe103)]
-   **LOLCODE**: \* Make strings greedy Golmote [[`1a5e7a4`](https://github.com/PrismJS/prism/commit/1a5e7a4)]
-   **Markup**: \* Support HTML entities in attribute values ([#1143](https://github.com/PrismJS/prism/issues/1143)) [[`1d5047d`](https://github.com/PrismJS/prism/commit/1d5047d)]
-   **NSIS**:
    _ Update patterns ([#1033](https://github.com/PrismJS/prism/issues/1033)) [[`01a59d8`](https://github.com/PrismJS/prism/commit/01a59d8)]
    _ Add support for NSIS 3.02 ([#1169](https://github.com/PrismJS/prism/issues/1169)) [[`393b5f7`](https://github.com/PrismJS/prism/commit/393b5f7)]
-   **PHP**:
    _ Fix the PHP language ([#1100](https://github.com/PrismJS/prism/issues/1100)) [[`1453fa7`](https://github.com/PrismJS/prism/commit/1453fa7)]
    _ Check for possible pre-existing marker strings in PHP [[`36bc560`](https://github.com/PrismJS/prism/commit/36bc560)]
-   **Ruby**:
    _ Fix slash regex performance. Fix [#1083](https://github.com/PrismJS/prism/issues/1083) [[`a708730`](https://github.com/PrismJS/prism/commit/a708730)]
    _ Add support for =begin =end comments. Manual merge of [#1121](https://github.com/PrismJS/prism/issues/1121). [[`62cdaf8`](https://github.com/PrismJS/prism/commit/62cdaf8)]
-   **Smarty**: \* Check for possible pre-existing marker strings in Smarty [[`5df26e2`](https://github.com/PrismJS/prism/commit/5df26e2)]
-   **TypeScript**:
    _ Update typescript keywords ([#1064](https://github.com/PrismJS/prism/issues/1064)) [[`52020a0`](https://github.com/PrismJS/prism/commit/52020a0)]
    _ Chmod -x prism-typescript component ([#1145](https://github.com/PrismJS/prism/issues/1145)) [[`afe0542`](https://github.com/PrismJS/prism/commit/afe0542)]
-   **YAML**: \* Make strings greedy (partial fix for [#1075](https://github.com/PrismJS/prism/issues/1075)) [[`565a2cc`](https://github.com/PrismJS/prism/commit/565a2cc)]

### Updated plugins

-   **Autolinker**: \* Fixed an rendering issue for encoded urls ([#1173](https://github.com/PrismJS/prism/issues/1173)) [[`abc007f`](https://github.com/PrismJS/prism/commit/abc007f)]
-   **Custom Class**:
    _ Add missing noCSS property for the Custom Class plugin [[`ba64f8d`](https://github.com/PrismJS/prism/commit/ba64f8d)]
    _ Added a default for classMap. Fixes [#1137](https://github.com/PrismJS/prism/issues/1137). ([#1157](https://github.com/PrismJS/prism/issues/1157)) [[`5400af9`](https://github.com/PrismJS/prism/commit/5400af9)]
-   **Keep Markup**: \* Store highlightedCode after reinserting markup. Fix [#1127](https://github.com/PrismJS/prism/issues/1127) [[`6df2ceb`](https://github.com/PrismJS/prism/commit/6df2ceb)]
-   **Line Highlight**:
    _ Cleanup left-over line-highlight tags before other plugins run [[`79b723d`](https://github.com/PrismJS/prism/commit/79b723d)]
    _ Avoid conflict between line-highlight and other plugins [[`224fdb8`](https://github.com/PrismJS/prism/commit/224fdb8)]
-   **Line Numbers**:
    _ Support soft wrap for line numbers plugin ([#584](https://github.com/PrismJS/prism/issues/584)) [[`849f1d6`](https://github.com/PrismJS/prism/commit/849f1d6)]
    _ Plugins fixes (unescaped-markup, line-numbers) ([#1012](https://github.com/PrismJS/prism/issues/1012)) [[`3fb7cf8`](https://github.com/PrismJS/prism/commit/3fb7cf8)]
-   **Normalize Whitespace**: \* Add Node.js support for the normalize-whitespace plugin [[`6c7dae2`](https://github.com/PrismJS/prism/commit/6c7dae2)]
-   **Unescaped Markup**: \* Plugins fixes (unescaped-markup, line-numbers) ([#1012](https://github.com/PrismJS/prism/issues/1012)) [[`3fb7cf8`](https://github.com/PrismJS/prism/commit/3fb7cf8)]

### Updated themes

-   **Coy**: \* Scroll 'Coy' background with contents ([#1163](https://github.com/PrismJS/prism/issues/1163)) [[`310990b`](https://github.com/PrismJS/prism/commit/310990b)]

### Other changes

-   Initial implementation of manual highlighting ([#1087](https://github.com/PrismJS/prism/issues/1087)) [[`bafc4cb`](https://github.com/PrismJS/prism/commit/bafc4cb)]
-   Remove dead link in Third-party tutorials section. Fixes [#1028](https://github.com/PrismJS/prism/issues/1028) [[`dffadc6`](https://github.com/PrismJS/prism/commit/dffadc6)]
-   Most languages now use the greedy flag for better highlighting [[`7549ecc`](https://github.com/PrismJS/prism/commit/7549ecc)]
-   .npmignore: Unignore components.js ([#1108](https://github.com/PrismJS/prism/issues/1108)) [[`1f699e7`](https://github.com/PrismJS/prism/commit/1f699e7)]
-   Run before-highlight and after-highlight hooks even when no grammar is found. Fix [#1134](https://github.com/PrismJS/prism/issues/1134) [[`70cb472`](https://github.com/PrismJS/prism/commit/70cb472)]
-   Replace [\w\W] with [\s\S] and [0-9] with \d in regexes ([#1107](https://github.com/PrismJS/prism/issues/1107)) [[`8aa2cc4`](https://github.com/PrismJS/prism/commit/8aa2cc4)]
-   Fix corner cases for the greedy flag ([#1095](https://github.com/PrismJS/prism/issues/1095)) [[`6530709`](https://github.com/PrismJS/prism/commit/6530709)]
-   Add Third Party Tutorial ([#1156](https://github.com/PrismJS/prism/issues/1156)) [[`c34e57b`](https://github.com/PrismJS/prism/commit/c34e57b)]
-   Add Composer support ([#648](https://github.com/PrismJS/prism/issues/648)) [[`2989633`](https://github.com/PrismJS/prism/commit/2989633)]
-   Remove IE8 plugin ([#992](https://github.com/PrismJS/prism/issues/992)) [[`25788eb`](https://github.com/PrismJS/prism/commit/25788eb)]
-   Website: remove width and height on logo.svg, so it becomes scalable. Close [#1005](https://github.com/PrismJS/prism/issues/1005) [[`0621ff7`](https://github.com/PrismJS/prism/commit/0621ff7)]
-   Remove yarn.lock ([#1098](https://github.com/PrismJS/prism/issues/1098)) [[`11eed25`](https://github.com/PrismJS/prism/commit/11eed25)]

## 1.6.0 (2016-12-03)

### New components

-   **.properties** ([#980](https://github.com/PrismJS/prism/issues/980)) [[`be6219a`](https://github.com/PrismJS/prism/commit/be6219a)]
-   **Ada** ([#949](https://github.com/PrismJS/prism/issues/949)) [[`65619f7`](https://github.com/PrismJS/prism/commit/65619f7)]
-   **GraphQL** ([#971](https://github.com/PrismJS/prism/issues/971)) [[`e018087`](https://github.com/PrismJS/prism/commit/e018087)]
-   **Jolie** ([#1014](https://github.com/PrismJS/prism/issues/1014)) [[`dfc1941`](https://github.com/PrismJS/prism/commit/dfc1941)]
-   **LiveScript** ([#982](https://github.com/PrismJS/prism/issues/982)) [[`62e258c`](https://github.com/PrismJS/prism/commit/62e258c)]
-   **Reason** (Fixes [#1046](https://github.com/PrismJS/prism/issues/1046)) [[`3cae6ce`](https://github.com/PrismJS/prism/commit/3cae6ce)]
-   **Xojo** ([#994](https://github.com/PrismJS/prism/issues/994)) [[`0224b7c`](https://github.com/PrismJS/prism/commit/0224b7c)]

### Updated components

-   **APL**: \* Add iota underbar ([#1024](https://github.com/PrismJS/prism/issues/1024)) [[`3c5c89a`](https://github.com/PrismJS/prism/commit/3c5c89a), [`ac21d33`](https://github.com/PrismJS/prism/commit/ac21d33)]
-   **AsciiDoc**: \* Optimized block regexps to prevent struggling on large files. Fixes [#1001](https://github.com/PrismJS/prism/issues/1001). [[`1a86d34`](https://github.com/PrismJS/prism/commit/1a86d34)]
-   **Bash**: \* Add `npm` to function list ([#969](https://github.com/PrismJS/prism/issues/969)) [[`912bdfe`](https://github.com/PrismJS/prism/commit/912bdfe)]
-   **CSS**: \* Make CSS strings greedy. Fix [#1013](https://github.com/PrismJS/prism/issues/1013). [[`e57e26d`](https://github.com/PrismJS/prism/commit/e57e26d)]
-   **CSS Extras**: \* Match attribute inside selectors [[`13fed76`](https://github.com/PrismJS/prism/commit/13fed76)]
-   \_Groovy\_\_: \* Fix order of decoding entities in groovy. Fixes [#1049](https://github.com/PrismJS/prism/issues/1049) ([#1050](https://github.com/PrismJS/prism/issues/1050)) [[`d75da8e`](https://github.com/PrismJS/prism/commit/d75da8e)]
-   **Ini**: \* Remove important token in ini definition ([#1047](https://github.com/PrismJS/prism/issues/1047)) [[`fe8ad8b`](https://github.com/PrismJS/prism/commit/fe8ad8b)]
-   **JavaScript**: \* Add exponentiation & spread/rest operator ([#991](https://github.com/PrismJS/prism/issues/991)) [[`b2de65a`](https://github.com/PrismJS/prism/commit/b2de65a), [`268d01e`](https://github.com/PrismJS/prism/commit/268d01e)]
-   \__JSON_: \* JSON: Fixed issues with properties and strings + added tests. Fix [#1025](https://github.com/PrismJS/prism/issues/1025) [[`25a541d`](https://github.com/PrismJS/prism/commit/25a541d)]
-   **Markup**:
    _ Allow for dots in Markup tag names, but not in HTML tags included in Textile. Fixes [#888](https://github.com/PrismJS/prism/issues/888). [[`31ea66b`](https://github.com/PrismJS/prism/commit/31ea66b)]
    _ Make doctype case-insensitive ([#1009](https://github.com/PrismJS/prism/issues/1009)) [[`3dd7219`](https://github.com/PrismJS/prism/commit/3dd7219)]
-   **NSIS**: \* Updated patterns ([#1032](https://github.com/PrismJS/prism/issues/1032)) [[`76ba1b8`](https://github.com/PrismJS/prism/commit/76ba1b8)]
-   **PHP**: \* Make comments greedy. Fix [#197](https://github.com/PrismJS/prism/issues/197) [[`318aab3`](https://github.com/PrismJS/prism/commit/318aab3)]
-   **PowerShell**: \* Fix highlighting of empty comments ([#977](https://github.com/PrismJS/prism/issues/977)) [[`4fda477`](https://github.com/PrismJS/prism/commit/4fda477)]
-   **Puppet**: \* Fix over-greedy regexp detection ([#978](https://github.com/PrismJS/prism/issues/978)) [[`105be25`](https://github.com/PrismJS/prism/commit/105be25)]
-   **Ruby**:
    _ Fix typo `Fload` to `Float` in prism-ruby.js ([#1023](https://github.com/PrismJS/prism/issues/1023)) [[`22cb018`](https://github.com/PrismJS/prism/commit/22cb018)]
    _ Make strings greedy. Fixes [#1048](https://github.com/PrismJS/prism/issues/1048) [[`8b0520a`](https://github.com/PrismJS/prism/commit/8b0520a)]
-   **SCSS**:
    _ Alias statement as keyword. Fix [#246](https://github.com/PrismJS/prism/issues/246) [[`fd09391`](https://github.com/PrismJS/prism/commit/fd09391)]
    _ Highlight variables inside selectors and properties. [[`d6b5c2f`](https://github.com/PrismJS/prism/commit/d6b5c2f)] \* Highlight parent selector [[`8f5f1fa`](https://github.com/PrismJS/prism/commit/8f5f1fa)]
-   **TypeScript**: \* Add missing `from` keyword to typescript & set `ts` as alias. ([#1042](https://github.com/PrismJS/prism/issues/1042)) [[`cba78f3`](https://github.com/PrismJS/prism/commit/cba78f3)]

### New plugins

-   **Copy to Clipboard** ([#891](https://github.com/PrismJS/prism/issues/891)) [[`07b81ac`](https://github.com/PrismJS/prism/commit/07b81ac)]
-   **Custom Class** ([#950](https://github.com/PrismJS/prism/issues/950)) [[`a0bd686`](https://github.com/PrismJS/prism/commit/a0bd686)]
-   **Data-URI Highlight** ([#996](https://github.com/PrismJS/prism/issues/996)) [[`bdca61b`](https://github.com/PrismJS/prism/commit/bdca61b)]
-   **Toolbar** ([#891](https://github.com/PrismJS/prism/issues/891)) [[`07b81ac`](https://github.com/PrismJS/prism/commit/07b81ac)]

### Updated plugins

-   **Autoloader**: \* Updated documentation for Autoloader plugin [[`b4f3423`](https://github.com/PrismJS/prism/commit/b4f3423)]
    -   Download all grammars as a zip from Autoloader plugin page ([#981](https://github.com/PrismJS/prism/issues/981)) [[`0d0a007`](https://github.com/PrismJS/prism/commit/0d0a007), [`5c815d3`](https://github.com/PrismJS/prism/commit/5c815d3)]
    -   Removed duplicated script on Autoloader plugin page [[`9671996`](https://github.com/PrismJS/prism/commit/9671996)]
    -   Don't try to load "none" component. Fix [#1000](https://github.com/PrismJS/prism/issues/1000) [[`f89b0b9`](https://github.com/PrismJS/prism/commit/f89b0b9)]
-   **WPD**: \* Fix at-rule detection + don't process if language is not handled [[`2626728`](https://github.com/PrismJS/prism/commit/2626728)]

### Other changes

-   Improvement to greedy-flag ([#967](https://github.com/PrismJS/prism/issues/967)) [[`500121b`](https://github.com/PrismJS/prism/commit/500121b), [`9893489`](https://github.com/PrismJS/prism/commit/9893489)]
-   Add setTimeout fallback for requestAnimationFrame. Fixes [#987](https://github.com/PrismJS/prism/issues/987). ([#988](https://github.com/PrismJS/prism/issues/988)) [[`c9bdcd3`](https://github.com/PrismJS/prism/commit/c9bdcd3)]
-   Added aria-hidden attributes on elements created by the Line Highlight and Line Numbers plugins. Fixes [#574](https://github.com/PrismJS/prism/issues/574). [[`e5587a7`](https://github.com/PrismJS/prism/commit/e5587a7)]
-   Don't insert space before ">" when there is no attributes [[`3dc8c9e`](https://github.com/PrismJS/prism/commit/3dc8c9e)]
-   Added missing hooks-related tests for AsciiDoc, Groovy, Handlebars, Markup, PHP and Smarty [[`c1a0c1b`](https://github.com/PrismJS/prism/commit/c1a0c1b)]
-   Fix issue when using Line numbers plugin and Normalise whitespace plugin together with Handlebars, PHP or Smarty. Fix [#1018](https://github.com/PrismJS/prism/issues/1018), [#997](https://github.com/PrismJS/prism/issues/997), [#935](https://github.com/PrismJS/prism/issues/935). Revert [#998](https://github.com/PrismJS/prism/issues/998). [[`86aa3d2`](https://github.com/PrismJS/prism/commit/86aa3d2)]
-   Optimized logo ([#990](https://github.com/PrismJS/prism/issues/990)) ([#1002](https://github.com/PrismJS/prism/issues/1002)) [[`f69e570`](https://github.com/PrismJS/prism/commit/f69e570), [`218fd25`](https://github.com/PrismJS/prism/commit/218fd25)]
-   Remove unneeded prefixed CSS ([#989](https://github.com/PrismJS/prism/issues/989)) [[`5e56833`](https://github.com/PrismJS/prism/commit/5e56833)]
-   Optimize images ([#1007](https://github.com/PrismJS/prism/issues/1007)) [[`b2fa6d5`](https://github.com/PrismJS/prism/commit/b2fa6d5)]
-   Add yarn.lock to .gitignore ([#1035](https://github.com/PrismJS/prism/issues/1035)) [[`03ecf74`](https://github.com/PrismJS/prism/commit/03ecf74)]
-   Fix greedy flag bug. Fixes [#1039](https://github.com/PrismJS/prism/issues/1039) [[`32cd99f`](https://github.com/PrismJS/prism/commit/32cd99f)]
-   Ruby: Fix test after [#1023](https://github.com/PrismJS/prism/issues/1023) [[`b15d43b`](https://github.com/PrismJS/prism/commit/b15d43b)]
-   Ini: Fix test after [#1047](https://github.com/PrismJS/prism/issues/1047) [[`25cdd3f`](https://github.com/PrismJS/prism/commit/25cdd3f)]
-   Reduce risk of XSS ([#1051](https://github.com/PrismJS/prism/issues/1051)) [[`17e33bc`](https://github.com/PrismJS/prism/commit/17e33bc)]
-   env.code can be modified by before-sanity-check hook even when using language-none. Fix [#1066](https://github.com/PrismJS/prism/issues/1066) [[`83bafbd`](https://github.com/PrismJS/prism/commit/83bafbd)]

## 1.5.1 (2016-06-05)

### Updated components

-   **Normalize Whitespace**: \* Add class that disables the normalize whitespace plugin [[`9385c54`](https://github.com/PrismJS/prism/commit/9385c54)]
-   **JavaScript Language**: \* Rearrange the `string` and `template-string` token in JavaScript [[`1158e46`](https://github.com/PrismJS/prism/commit/1158e46)]
-   **SQL Language**:
    _ add delimeter and delimeters keywords to sql ([#958](https://github.com/PrismJS/prism/pull/958)) [[`a9ef24e`](https://github.com/PrismJS/prism/commit/a9ef24e)]
    _ add AUTO_INCREMENT and DATE keywords to sql ([#954](https://github.com/PrismJS/prism/pull/954)) [[`caea2af`](https://github.com/PrismJS/prism/commit/caea2af)]
-   **Diff Language**: \* Highlight diff lines with only + or - ([#952](https://github.com/PrismJS/prism/pull/952)) [[`4d0526f`](https://github.com/PrismJS/prism/commit/4d0526f)]

### Other changes

-   Allow for asynchronous loading of prism.js ([#959](https://github.com/PrismJS/prism/pull/959))
-   Use toLowerCase on language names ([#957](https://github.com/PrismJS/prism/pull/957)) [[`acd9508`](https://github.com/PrismJS/prism/commit/acd9508)]
-   link to index for basic usage - fixes [#945](https://github.com/PrismJS/prism/issues/945) ([#946](https://github.com/PrismJS/prism/pull/946)) [[`6c772d8`](https://github.com/PrismJS/prism/commit/6c772d8)]
-   Fixed monospace typo ([#953](https://github.com/PrismJS/prism/pull/953)) [[`e6c3498`](https://github.com/PrismJS/prism/commit/e6c3498)]

## 1.5.0 (2016-05-01)

### New components

-   **Bro Language** ([#925](https://github.com/PrismJS/prism/pull/925))
-   **Protocol Buffers Language** ([#938](https://github.com/PrismJS/prism/pull/938)) [[`ae4a4f2`](https://github.com/PrismJS/prism/commit/ae4a4f2)]

### Updated components

-   **Keep Markup**: \* Fix Keep Markup plugin incorrect highlighting ([#880](https://github.com/PrismJS/prism/pull/880)) [[`24841ef`](https://github.com/PrismJS/prism/commit/24841ef)]
-   **Groovy Language**: \* Fix double HTML-encoding bug in Groovy language [[`24a0936`](https://github.com/PrismJS/prism/commit/24a0936)]
-   **Java Language**: \* Adding annotation token for Java ([#905](https://github.com/PrismJS/prism/pull/905)) [[`367ace6`](https://github.com/PrismJS/prism/commit/367ace6)]
-   **SAS Language**: \* Add missing keywords for SAS ([#922](https://github.com/PrismJS/prism/pull/922))
-   **YAML Language**: \* fix hilighting of YAML keys on first line of code block ([#943](https://github.com/PrismJS/prism/pull/943)) [[`f19db81`](https://github.com/PrismJS/prism/commit/f19db81)]
-   **C# Language**: \* Support for generic methods in csharp [[`6f75735`](https://github.com/PrismJS/prism/commit/6f75735)]

### New plugins

-   **Unescaped Markup** [[`07d77e5`](https://github.com/PrismJS/prism/commit/07d77e5)]
-   **Normalize Whitespace** ([#847](https://github.com/PrismJS/prism/pull/847)) [[`e86ec01`](https://github.com/PrismJS/prism/commit/e86ec01)]

### Other changes

-   Add JSPM support [[`ad048ab`](https://github.com/PrismJS/prism/commit/ad048ab)]
-   update linear-gradient syntax from `left` to `to right` [[`cd234dc`](https://github.com/PrismJS/prism/commit/cd234dc)]
-   Add after-property to allow ordering of plugins [[`224b7a1`](https://github.com/PrismJS/prism/commit/224b7a1)]
-   Partial solution for the "Comment-like substrings"-problem [[`2705c50`](https://github.com/PrismJS/prism/commit/2705c50)]
-   Add property 'aliasTitles' to components.js [[`54400fb`](https://github.com/PrismJS/prism/commit/54400fb)]
-   Add before-highlightall hook [[`70a8602`](https://github.com/PrismJS/prism/commit/70a8602)]
-   Fix catastrophic backtracking regex issues in JavaScript [[`ab65be2`](https://github.com/PrismJS/prism/commit/ab65be2)]

## 1.4.1 (2016-02-03)

### Other changes

-   Fix DFS bug in Prism core [[`b86c727`](https://github.com/PrismJS/prism/commit/b86c727)]

## 1.4.0 (2016-02-03)

### New components

-   **Solarized Light** ([#855](https://github.com/PrismJS/prism/pull/855)) [[`70846ba`](https://github.com/PrismJS/prism/commit/70846ba)]
-   **JSON** ([#370](https://github.com/PrismJS/prism/pull/370)) [[`ad2fcd0`](https://github.com/PrismJS/prism/commit/ad2fcd0)]

### Updated components

-   **Show Language**:
    _ Remove data-language attribute ([#840](https://github.com/PrismJS/prism/pull/840)) [[`eb9a83c`](https://github.com/PrismJS/prism/commit/eb9a83c)]
    _ Allow custom label without a language mapping ([#837](https://github.com/PrismJS/prism/pull/837)) [[`7e74aef`](https://github.com/PrismJS/prism/commit/7e74aef)]
-   **JSX**: \* Better Nesting in JSX attributes ([#842](https://github.com/PrismJS/prism/pull/842)) [[`971dda7`](https://github.com/PrismJS/prism/commit/971dda7)]
-   **File Highlight**: \* Defer File Highlight until the full DOM has loaded. ([#844](https://github.com/PrismJS/prism/pull/844)) [[`6f995ef`](https://github.com/PrismJS/prism/commit/6f995ef)]
-   **Coy Theme**: \* Fix coy theme shadows ([#865](https://github.com/PrismJS/prism/pull/865)) [[`58d2337`](https://github.com/PrismJS/prism/commit/58d2337)]
-   **Show Invisibles**:
    _ Ensure show-invisibles compat with autoloader ([#874](https://github.com/PrismJS/prism/pull/874)) [[`c3cfb1f`](https://github.com/PrismJS/prism/commit/c3cfb1f)]
    _ Add support for the space character for the show-invisibles plugin ([#876](https://github.com/PrismJS/prism/pull/876)) [[`05442d3`](https://github.com/PrismJS/prism/commit/05442d3)]

### New plugins

-   **Command Line** ([#831](https://github.com/PrismJS/prism/pull/831)) [[`8378906`](https://github.com/PrismJS/prism/commit/8378906)]

### Other changes

-   Use document.currentScript instead of document.getElementsByTagName() [[`fa98743`](https://github.com/PrismJS/prism/commit/fa98743)]
-   Add prefix for Firefox selection and move prefixed rule first [[`6d54717`](https://github.com/PrismJS/prism/commit/6d54717)]
-   No background for `<code>` in `<pre>` [[`8c310bc`](https://github.com/PrismJS/prism/commit/8c310bc)]
-   Fixing to initial copyright year [[`69cbf7a`](https://github.com/PrismJS/prism/commit/69cbf7a)]
-   Simplify the “lang” regex [[`417f54a`](https://github.com/PrismJS/prism/commit/417f54a)]
-   Fix broken heading links [[`a7f9e62`](https://github.com/PrismJS/prism/commit/a7f9e62)]
-   Prevent infinite recursion in DFS [[`02894e1`](https://github.com/PrismJS/prism/commit/02894e1)]
-   Fix incorrect page title [[`544b56f`](https://github.com/PrismJS/prism/commit/544b56f)]
-   Link scss to webplatform wiki [[`08d979a`](https://github.com/PrismJS/prism/commit/08d979a)]
-   Revert white-space to normal when code is inline instead of in a pre [[`1a971b5`](https://github.com/PrismJS/prism/commit/1a971b5)]

## 1.3.0 (2015-10-26)

### New components

-   **AsciiDoc** ([#800](https://github.com/PrismJS/prism/issues/800)) [[`6803ca0`](https://github.com/PrismJS/prism/commit/6803ca0)]
-   **Haxe** ([#811](https://github.com/PrismJS/prism/issues/811)) [[`bd44341`](https://github.com/PrismJS/prism/commit/bd44341)]
-   **Icon** ([#803](https://github.com/PrismJS/prism/issues/803)) [[`b43c5f3`](https://github.com/PrismJS/prism/commit/b43c5f3)]
-   \_\_Kotlin ([#814](https://github.com/PrismJS/prism/issues/814)) [[`e8a31a5`](https://github.com/PrismJS/prism/commit/e8a31a5)]
-   **Lua** ([#804](https://github.com/PrismJS/prism/issues/804)) [[`a36bc4a`](https://github.com/PrismJS/prism/commit/a36bc4a)]
-   **Nix** ([#795](https://github.com/PrismJS/prism/issues/795)) [[`9b275c8`](https://github.com/PrismJS/prism/commit/9b275c8)]
-   **Oz** ([#805](https://github.com/PrismJS/prism/issues/805)) [[`388c53f`](https://github.com/PrismJS/prism/commit/388c53f)]
-   **PARI/GP** ([#802](https://github.com/PrismJS/prism/issues/802)) [[`253c035`](https://github.com/PrismJS/prism/commit/253c035)]
-   **Parser** ([#808](https://github.com/PrismJS/prism/issues/808)) [[`a953b3a`](https://github.com/PrismJS/prism/commit/a953b3a)]
-   **Puppet** ([#813](https://github.com/PrismJS/prism/issues/813)) [[`81933ee`](https://github.com/PrismJS/prism/commit/81933ee)]
-   **Roboconf** ([#812](https://github.com/PrismJS/prism/issues/812)) [[`f5db346`](https://github.com/PrismJS/prism/commit/f5db346)]

### Updated components

-   **C**: \* Highlight directives in preprocessor lines ([#801](https://github.com/PrismJS/prism/issues/801)) [[`ad316a3`](https://github.com/PrismJS/prism/commit/ad316a3)]
-   **C#**:
    _ Highlight directives in preprocessor lines ([#801](https://github.com/PrismJS/prism/issues/801)) [[`ad316a3`](https://github.com/PrismJS/prism/commit/ad316a3)]
    _ Fix detection of float numbers ([#806](https://github.com/PrismJS/prism/issues/806)) [[`1dae72b`](https://github.com/PrismJS/prism/commit/1dae72b)]
-   **F#**: \* Highlight directives in preprocessor lines ([#801](https://github.com/PrismJS/prism/issues/801)) [[`ad316a3`](https://github.com/PrismJS/prism/commit/ad316a3)]
-   **JavaScript**: \* Highlight true and false as booleans ([#801](https://github.com/PrismJS/prism/issues/801)) [[`ad316a3`](https://github.com/PrismJS/prism/commit/ad316a3)]
-   **Python**: \* Highlight triple-quoted strings before comments. Fix [#815](https://github.com/PrismJS/prism/issues/815) [[`90fbf0b`](https://github.com/PrismJS/prism/commit/90fbf0b)]

### New plugins

-   **Previewer: Time** ([#790](https://github.com/PrismJS/prism/issues/790)) [[`88173de`](https://github.com/PrismJS/prism/commit/88173de)]
-   **Previewer: Angle** ([#791](https://github.com/PrismJS/prism/issues/791)) [[`a434c86`](https://github.com/PrismJS/prism/commit/a434c86)]

### Other changes

-   Increase mocha's timeout [[`f1c41db`](https://github.com/PrismJS/prism/commit/f1c41db)]
-   Prevent most errors in IE8. Fix [#9](https://github.com/PrismJS/prism/issues/9) [[`9652d75`](https://github.com/PrismJS/prism/commit/9652d75)]
-   Add U.S. Web Design Standards on homepage. Fix [#785](https://github.com/PrismJS/prism/issues/785) [[`e10d48b`](https://github.com/PrismJS/prism/commit/e10d48b), [`79ebbf8`](https://github.com/PrismJS/prism/commit/79ebbf8), [`2f7088d`](https://github.com/PrismJS/prism/commit/2f7088d)]
-   Added gulp task to autolink PRs and commits in changelog [[`5ec4e4d`](https://github.com/PrismJS/prism/commit/5ec4e4d)]
-   Use child processes to run each set of tests, in order to deal with the memory leak in vm.runInNewContext() [[`9a4b6fa`](https://github.com/PrismJS/prism/commit/9a4b6fa)]

## 1.2.0 (2015-10-07)

### New components

-   **Batch** ([#781](https://github.com/PrismJS/prism/issues/781)) [[`eab5b06`](https://github.com/PrismJS/prism/commit/eab5b06)]

### Updated components

-   **ASP.NET**: \* Simplified pattern for `<script>` [[`29643f4`](https://github.com/PrismJS/prism/issues/29643f4)]
-   **Bash**:
    _ Fix regression in strings ([#792](https://github.com/PrismJS/prism/issues/792)) [[`bd275c2`](https://github.com/PrismJS/prism/commit/bd275c2)]
    _ Substantially reduce wrongly highlighted stuff ([#793](https://github.com/PrismJS/prism/issues/793)) [[`ac6fe2e`](https://github.com/PrismJS/prism/commit/ac6fe2e)]
-   **CSS**: \* Simplified pattern for `<style>` [[`29643f4`](https://github.com/PrismJS/prism/issues/29643f4)]
-   **JavaScript**: \* Simplified pattern for `<script>` [[`29643f4`](https://github.com/PrismJS/prism/issues/29643f4)]

### New plugins

-   **Previewer: Gradient** ([#783](https://github.com/PrismJS/prism/issues/783)) [[`9a63483`](https://github.com/PrismJS/prism/commit/9a63483)]

### Updated plugins

-   **Previewer: Color** \* Add support for Sass variables [[`3a1fb04`](https://github.com/PrismJS/prism/commit/3a1fb04)]

-   **Previewer: Easing** \* Add support for Sass variables [[`7c7ab4e`](https://github.com/PrismJS/prism/commit/7c7ab4e)]

### Other changes

-   Test runner: Allow to run tests for only some languages [[`5ade8a5`](https://github.com/PrismJS/prism/issues/5ade8a5)]
-   Download page: Fixed wrong components order raising error in generated file ([#797](https://github.com/PrismJS/prism/issues/787)) [[`7a6aed8`](https://github.com/PrismJS/prism/commit/7a6aed8)]

## 1.1.0 (2015-10-04)

### New components

-   **ABAP** ([#636](https://github.com/PrismJS/prism/issues/636)) [[`75b0328`](https://github.com/PrismJS/prism/commit/75b0328), [`0749129`](https://github.com/PrismJS/prism/commit/0749129)]
-   **APL** ([#308](https://github.com/PrismJS/prism/issues/308)) [[`1f45942`](https://github.com/PrismJS/prism/commit/1f45942), [`33a295f`](https://github.com/PrismJS/prism/commit/33a295f)]
-   **AutoIt** ([#771](https://github.com/PrismJS/prism/issues/771)) [[`211a41c`](https://github.com/PrismJS/prism/commit/211a41c)]
-   **BASIC** ([#620](https://github.com/PrismJS/prism/issues/620)) [[`805a0ce`](https://github.com/PrismJS/prism/commit/805a0ce)]
-   **Bison** ([#764](https://github.com/PrismJS/prism/issues/764)) [[`7feb135`](https://github.com/PrismJS/prism/commit/7feb135)]
-   **Crystal** ([#780](https://github.com/PrismJS/prism/issues/780)) [[`5b473de`](https://github.com/PrismJS/prism/commit/5b473de), [`414848d`](https://github.com/PrismJS/prism/commit/414848d)]
-   **D** ([#613](https://github.com/PrismJS/prism/issues/613)) [[`b5e741c`](https://github.com/PrismJS/prism/commit/b5e741c)]
-   **Diff** ([#450](https://github.com/PrismJS/prism/issues/450)) [[`ef41c74`](https://github.com/PrismJS/prism/commit/ef41c74)]
-   **Docker** ([#576](https://github.com/PrismJS/prism/issues/576)) [[`e808352`](https://github.com/PrismJS/prism/commit/e808352)]
-   **Elixir** ([#614](https://github.com/PrismJS/prism/issues/614)) [[`a1c028c`](https://github.com/PrismJS/prism/commit/a1c028c), [`c451611`](https://github.com/PrismJS/prism/commit/c451611), [`2e637f0`](https://github.com/PrismJS/prism/commit/2e637f0), [`ccb6566`](https://github.com/PrismJS/prism/commit/ccb6566)]
-   **GLSL** ([#615](https://github.com/PrismJS/prism/issues/615)) [[`247da05`](https://github.com/PrismJS/prism/commit/247da05)]
-   **Inform 7** ([#616](https://github.com/PrismJS/prism/issues/616)) [[`d2595b4`](https://github.com/PrismJS/prism/commit/d2595b4)]
-   **J** ([#623](https://github.com/PrismJS/prism/issues/623)) [[`0cc50b2`](https://github.com/PrismJS/prism/commit/0cc50b2)]
-   **MEL** ([#618](https://github.com/PrismJS/prism/issues/618)) [[`8496c14`](https://github.com/PrismJS/prism/commit/8496c14)]
-   **Mizar** ([#619](https://github.com/PrismJS/prism/issues/619)) [[`efde61d`](https://github.com/PrismJS/prism/commit/efde61d)]
-   **Monkey** ([#621](https://github.com/PrismJS/prism/issues/621)) [[`fdd4a3c`](https://github.com/PrismJS/prism/commit/fdd4a3c)]
-   **nginx** ([#776](https://github.com/PrismJS/prism/issues/776)) [[`dc4fc19`](https://github.com/PrismJS/prism/commit/dc4fc19), [`e62c88e`](https://github.com/PrismJS/prism/commit/e62c88e)]
-   **Nim** ([#622](https://github.com/PrismJS/prism/issues/622)) [[`af9c49a`](https://github.com/PrismJS/prism/commit/af9c49a)]
-   **OCaml** ([#628](https://github.com/PrismJS/prism/issues/628)) [[`556c04d`](https://github.com/PrismJS/prism/commit/556c04d)]
-   **Processing** ([#629](https://github.com/PrismJS/prism/issues/629)) [[`e47087b`](https://github.com/PrismJS/prism/commit/e47087b)]
-   **Prolog** ([#630](https://github.com/PrismJS/prism/issues/630)) [[`dd04c32`](https://github.com/PrismJS/prism/commit/dd04c32)]
-   **Pure** ([#626](https://github.com/PrismJS/prism/issues/626)) [[`9c276ab`](https://github.com/PrismJS/prism/commit/9c276ab)]
-   **Q** ([#624](https://github.com/PrismJS/prism/issues/624)) [[`c053c9e`](https://github.com/PrismJS/prism/commit/c053c9e)]
-   **Qore** [[`125e91f`](https://github.com/PrismJS/prism/commit/125e91f)]
-   **Tcl** [[`a3e751a`](https://github.com/PrismJS/prism/commit/a3e751a), [`11ff829`](https://github.com/PrismJS/prism/commit/11ff829)]
-   **Textile** ([#544](https://github.com/PrismJS/prism/issues/544)) [[`d0c6764`](https://github.com/PrismJS/prism/commit/d0c6764)]
-   **Verilog** ([#640](https://github.com/PrismJS/prism/issues/640)) [[`44a11c2`](https://github.com/PrismJS/prism/commit/44a11c2), [`795eb99`](https://github.com/PrismJS/prism/commit/795eb99)]
-   **Vim** [[`69ea994`](https://github.com/PrismJS/prism/commit/69ea994)]

### Updated components

-   **Bash**:
    _ Add support for Here-Documents ([#787](https://github.com/PrismJS/prism/issues/787)) [[`b57a096`](https://github.com/PrismJS/prism/commit/b57a096)]
    _ Remove C-like dependency ([#789](https://github.com/PrismJS/prism/issues/789)) [[`1ab4619`](https://github.com/PrismJS/prism/commit/1ab4619)]
-   **C**: \* Fixed numbers [[`4d64d07`](https://github.com/PrismJS/prism/commit/4d64d07), [`071c3dd`](https://github.com/PrismJS/prism/commit/071c3dd)]
-   **C-like**:
    _ Add word boundary before class-name prefixes [[`aa757f6`](https://github.com/PrismJS/prism/commit/aa757f6)]
    _ Improved operator regex + add != and !== [[`135ee9d`](https://github.com/PrismJS/prism/commit/135ee9d)] \* Optimized string regexp [[`792e35c`](https://github.com/PrismJS/prism/commit/792e35c)]
-   **F#**:
    _ Fixed keywords containing exclamation mark [[`09f2005`](https://github.com/PrismJS/prism/commit/09f2005)]
    _ Improved string pattern [[`0101c89`](https://github.com/PrismJS/prism/commit/0101c89)]
    _ Insert preprocessor before keyword + don't allow line feeds before # [[`fdc9477`](https://github.com/PrismJS/prism/commit/fdc9477)]
    _ Fixed numbers [[`0aa0791`](https://github.com/PrismJS/prism/commit/0aa0791)]
-   **Gherkin**:
    _ Don't allow spaces in tags [[`48ff8b7`](https://github.com/PrismJS/prism/commit/48ff8b7)]
    _ Handle \r\n and \r + allow feature alone + don't match blank td/th [[`ce1ec3b`](https://github.com/PrismJS/prism/commit/ce1ec3b)]
-   **Git**:
    _ Added more examples ([#652](https://github.com/PrismJS/prism/issues/652)) [[`95dc102`](https://github.com/PrismJS/prism/commit/95dc102)]
    _ Add support for unified diff. Fixes [#769](https://github.com/PrismJS/prism/issues/769), fixes [#357](https://github.com/PrismJS/prism/issues/357), closes [#401](https://github.com/PrismJS/prism/issues/401) [[`3aadd5d`](https://github.com/PrismJS/prism/commit/3aadd5d)]
-   **Go**: \* Improved operator regexp + removed punctuation from it [[`776ab90`](https://github.com/PrismJS/prism/commit/776ab90)]
-   **Haml**:
    _ Combine both multiline-comment regexps + handle \r\n and \r [[`f77b40b`](https://github.com/PrismJS/prism/commit/f77b40b)]
    _ Handle \r\n and \r in filter regex [[`bbe68ac`](https://github.com/PrismJS/prism/commit/bbe68ac)]
-   **Handlebars**:
    _ Fix empty strings, add plus sign in exponential notation, improve block pattern and variable pattern [[`c477f9a`](https://github.com/PrismJS/prism/commit/c477f9a)]
    _ Properly escape special replacement patterns ($) in Handlebars, PHP and Smarty. Fix [#772](https://github.com/PrismJS/prism/issues/772) [[`895bf46`](https://github.com/PrismJS/prism/commit/895bf46)]
-   **Haskell**: \* Removed useless backslashes and parentheses + handle \r\n and \r + simplify number regexp + fix operator regexp [[`1cc8d8e`](https://github.com/PrismJS/prism/commit/1cc8d8e)]
-   **HTTP**: \* Fix indentation + Add multiline flag for more flexibility + Fix response status + Handle \r\n and \r [[`aaa90f1`](https://github.com/PrismJS/prism/commit/aaa90f1)]
-   **Ini**: \* Fix some regexps + remove unused flags [[`53d5839`](https://github.com/PrismJS/prism/commit/53d5839)]
-   **Jade**: \* Add todo list + remove single-line comment pattern + simplified most patterns with m flag + handle \r\n and \r [[`a79e838`](https://github.com/PrismJS/prism/commit/a79e838)]
-   **Java**: \* Fix number regexp + simplified number regexp and optimized operator regexp [[`21e20b9`](https://github.com/PrismJS/prism/commit/21e20b9)]
-   **JavaScript**: \* JavaScript: Allow for all non-ASCII characters in function names. Fix [#400](https://github.com/PrismJS/prism/issues/400) [[`29e26dc`](https://github.com/PrismJS/prism/commit/29e26dc)]
-   **JSX**: \* Allow for one level of nesting in scripts (Fix [#717](https://github.com/PrismJS/prism/issues/717)) [[`90c75d5`](https://github.com/PrismJS/prism/commit/90c75d5)]
-   **Julia**: \* Simplify comment regexp + improved number regexp + improved operator regexp [[`bcac7d4`](https://github.com/PrismJS/prism/commit/bcac7d4)]
-   **Keyman**: \* Move header statements above keywords [[`23a444c`](https://github.com/PrismJS/prism/commit/23a444c)]
-   **LaTeX**:
    _ Simplify comment regexp [[`132b41a`](https://github.com/PrismJS/prism/commit/132b41a)]
    _ Extend support [[`942a6ec`](https://github.com/PrismJS/prism/commit/942a6ec)]
-   **Less**: \* Remove useless part in property regexp [[`80d8260`](https://github.com/PrismJS/prism/commit/80d8260)]
-   **LOLCODE**: \* Removed useless parentheses [[`8147c9b`](https://github.com/PrismJS/prism/commit/8147c9b)]
-   **Makefile**:
    _ Add known failures in example [[`e0f8984`](https://github.com/PrismJS/prism/commit/e0f8984)]
    _ Handle \r\n in comments and strings + fix "-include" keyword
-   **Markup**:
    _ Simplify patterns + handle \r\n and \r [[`4c551e8`](https://github.com/PrismJS/prism/commit/4c551e8)]
    _ Don't allow = to appear in tag name [[`85d8a55`](https://github.com/PrismJS/prism/commit/85d8a55)] \* Don't allow dot inside tag name [[`283691e`](https://github.com/PrismJS/prism/commit/283691e)]
-   **MATLAB**: \* Simplify string pattern to remove lookbehind [[`a3cbecc`](https://github.com/PrismJS/prism/commit/a3cbecc)]
-   **NASM**: \* Converted indents to tabs, removed uneeded escapes, added lookbehinds [[`a92e4bd`](https://github.com/PrismJS/prism/commit/a92e4bd)]
-   **NSIS**:
    _ Simplified patterns [[`bbd83d4`](https://github.com/PrismJS/prism/commit/bbd83d4)]
    _ Fix operator regexp [[`44ad8dc`](https://github.com/PrismJS/prism/commit/44ad8dc)]
-   **Objective-C**:
    _ Simplified regexps + fix strings + handle \r [[`1d33147`](https://github.com/PrismJS/prism/commit/1d33147)]
    _ Fix operator regexp [[`e9d382e`](https://github.com/PrismJS/prism/commit/e9d382e)]
-   **Pascal**: \* Simplified regexps [[`c03c8a4`](https://github.com/PrismJS/prism/commit/c03c8a4)]
-   **Perl**: \* Simplified regexps + Made most string and regexp patterns multi-line + Added support for regexp's n flag + Added missing operators [[`71b00cc`](https://github.com/PrismJS/prism/commit/71b00cc)]
-   **PHP**:
    _ Simplified patterns [[`f9d9452`](https://github.com/PrismJS/prism/commit/f9d9452)]
    _ Properly escape special replacement patterns ($) in Handlebars, PHP and Smarty. Fix [#772](https://github.com/PrismJS/prism/issues/772) [[`895bf46`](https://github.com/PrismJS/prism/commit/895bf46)]
-   **PHP Extras**: \* Fix $this regexp + improve global regexp [[`781fdad`](https://github.com/PrismJS/prism/commit/781fdad)]
-   **PowerShell**: \* Update definitions for command/alias/operators [[`14da55c`](https://github.com/PrismJS/prism/commit/14da55c)]
-   **Python**:
    _ Added async/await and @ operator ([#656](https://github.com/PrismJS/prism/issues/656)) [[`7f1ae75`](https://github.com/PrismJS/prism/commit/7f1ae75)]
    _ Added 'self' keyword and support for class names ([#677](https://github.com/PrismJS/prism/issues/677)) [[`d9d4ab2`](https://github.com/PrismJS/prism/commit/d9d4ab2)] \* Simplified regexps + don't capture where unneeded + fixed operators [[`530f5f0`](https://github.com/PrismJS/prism/commit/530f5f0)]
-   **R**: \* Fixed and simplified patterns [[`c20c3ec`](https://github.com/PrismJS/prism/commit/c20c3ec)]
-   **reST**: \* Simplified some patterns, fixed others, prevented blank comments to match, moved list-bullet down to prevent breaking quotes [[`e6c6b85`](https://github.com/PrismJS/prism/commit/e6c6b85)]
-   **Rip**: \* Fixed some regexp + moved down numbers [[`1093f7d`](https://github.com/PrismJS/prism/commit/1093f7d)]
-   **Ruby**:
    _ Code cleaning, handle \r\n and \r, fix some regexps [[`dd4989f`](https://github.com/PrismJS/prism/commit/dd4989f)]
    _ Add % notations for strings and regexps. Fix [#590](https://github.com/PrismJS/prism/issues/590) [[`2d37800`](https://github.com/PrismJS/prism/commit/2d37800)]
-   **Rust**: \* Simplified patterns and fixed operators [[`6c8494f`](https://github.com/PrismJS/prism/commit/6c8494f)]
-   **SAS**: \* Simplified datalines and optimized operator patterns [[`6ebb96f`](https://github.com/PrismJS/prism/commit/6ebb96f)]
-   **Sass**:
    _ Add missing require in components [[`35b8c50`](https://github.com/PrismJS/prism/commit/35b8c50)]
    _ Fix comments, operators and selectors and simplified patterns [[`28759d0`](https://github.com/PrismJS/prism/commit/28759d0)] \* Highlight "-" as operator only if surrounded by spaces, in order to not break hyphenated values (e.g. "ease-in-out") [[`b2763e7`](https://github.com/PrismJS/prism/commit/b2763e7)]
-   **Scala**: \* Simplified patterns [[`daf2597`](https://github.com/PrismJS/prism/commit/daf2597)]
-   **Scheme**:
    _ Add missing lookbehind on number pattern. Fix [#702](https://github.com/PrismJS/prism/issues/702) [[`3120ff7`](https://github.com/PrismJS/prism/commit/3120ff7)]
    _ Fixes and simplifications [[`068704a`](https://github.com/PrismJS/prism/commit/068704a)] \* Don't match content of symbols starting with a parenthesis [[`fa7df08`](https://github.com/PrismJS/prism/commit/fa7df08)]
-   **Scss**: \* Simplified patterns + fixed operators + don't match empty selectors [[`672c167`](https://github.com/PrismJS/prism/commit/672c167)]
-   **Smalltalk**: \* Simplified patterns [[`d896622`](https://github.com/PrismJS/prism/commit/d896622)]
-   **Smarty**:
    _ Optimized regexps + fixed punctuation and operators [[`1446700`](https://github.com/PrismJS/prism/commit/1446700)]
    _ Properly escape special replacement patterns ($) in Handlebars, PHP and Smarty. Fix [#772](https://github.com/PrismJS/prism/issues/772) [[`895bf46`](https://github.com/PrismJS/prism/commit/895bf46)]
-   **SQL**: \* Simplified regexp + fixed keywords and operators + add CHARSET keyword [[`d49fec0`](https://github.com/PrismJS/prism/commit/d49fec0)]
-   **Stylus**: \* Rewrote the component entirely [[`7729728`](https://github.com/PrismJS/prism/commit/7729728)]
-   **Swift**:
    _ Optimized keywords lists and removed duplicates [[`936e429`](https://github.com/PrismJS/prism/commit/936e429)]
    _ Add support for string interpolation. Fix [#448](https://github.com/PrismJS/prism/issues/448) [[`89cd5d0`](https://github.com/PrismJS/prism/commit/89cd5d0)]
-   **Twig**:
    _ Prevent "other" pattern from matching blank strings [[`cae2cef`](https://github.com/PrismJS/prism/commit/cae2cef)]
    _ Optimized regexps + fixed operators + added missing operators/keywords [[`2d8271f`](https://github.com/PrismJS/prism/commit/2d8271f)]
-   **VHDL**: \* Move operator overloading before strings, don't capture if not needed, handle \r\n and \r, fix numbers [[`4533f17`](https://github.com/PrismJS/prism/commit/4533f17)]
-   **Wiki markup**: \* Fixed emphasis + merged some url patterns + added TODOs [[`8cf9e6a`](https://github.com/PrismJS/prism/commit/8cf9e6a)]
-   **YAML**: \* Handled \r\n and \r, simplified some patterns, fixed "---" [[`9e33e0a`](https://github.com/PrismJS/prism/commit/9e33e0a)]

### New plugins

-   **Autoloader** ([#766](https://github.com/PrismJS/prism/issues/766)) [[`ed4ccfe`](https://github.com/PrismJS/prism/commit/ed4ccfe)]
-   **JSONP Highlight** [[`b2f14d9`](https://github.com/PrismJS/prism/commit/b2f14d9)]
-   **Keep Markup** ([#770](https://github.com/PrismJS/prism/issues/770)) [[`bd3e9ea`](https://github.com/PrismJS/prism/commit/bd3e9ea)]
-   **Previewer: Base** ([#767](https://github.com/PrismJS/prism/issues/767)) [[`cf764c0`](https://github.com/PrismJS/prism/commit/cf764c0)]
-   **Previewer: Color** ([#767](https://github.com/PrismJS/prism/issues/767)) [[`cf764c0`](https://github.com/PrismJS/prism/commit/cf764c0)]
-   **Previewer: Easing** ([#773](https://github.com/PrismJS/prism/issues/773)) [[`513137c`](https://github.com/PrismJS/prism/commit/513137c), [`9207258`](https://github.com/PrismJS/prism/commit/9207258), [`4303c94`](https://github.com/PrismJS/prism/commit/4303c94)]
-   **Remove initial line feed** [[`ed9f2b2`](https://github.com/PrismJS/prism/commit/ed9f2b2), [`b8d098e`](https://github.com/PrismJS/prism/commit/b8d098e)]

### Updated plugins

-   **Autolinker**: \* Don't process all grammars on load, process each one in before-highlight. Should fix [#760](https://github.com/PrismJS/prism/issues/760) [[`a572495`](https://github.com/PrismJS/prism/commit/a572495)]
-   **Line Highlight**:
    _ Run in `complete` hook [[`f237e67`](https://github.com/PrismJS/prism/commit/f237e67)]
    _ Fixed position when font-size is odd ([#668](https://github.com/PrismJS/prism/issues/668)) [[`86bbd4c`](https://github.com/PrismJS/prism/commit/86bbd4c), [`8ed7ce3`](https://github.com/PrismJS/prism/commit/8ed7ce3)]
-   **Line Numbers**:
    _ Run in `complete` hook [[`3f4d918`](https://github.com/PrismJS/prism/commit/3f4d918)]
    _ Don't run if already exists [[`c89bbdb`](https://github.com/PrismJS/prism/commit/c89bbdb)]
    _ Don't run if block is empty. Fix [#669](https://github.com/PrismJS/prism/issues/669) [[`ee463e8`](https://github.com/PrismJS/prism/commit/ee463e8)]
    _ Correct calculation for number of lines (fix [#385](https://github.com/PrismJS/prism/issues/385)) [[`14f3f80`](https://github.com/PrismJS/prism/commit/14f3f80)]
    _ Fix computation of line numbers for single-line code blocks. Fix [#721](https://github.com/PrismJS/prism/issues/721) [[`02b220e`](https://github.com/PrismJS/prism/commit/02b220e)]
    _ Fixing word wrap on long code lines [[`56b3d29`](https://github.com/PrismJS/prism/commit/56b3d29)] \* Fixing coy theme + line numbers plugin overflowing on long blocks of text ([#762](https://github.com/PrismJS/prism/issues/762)) [[`a0127eb`](https://github.com/PrismJS/prism/commit/a0127eb)]
-   **Show Language**:
    _ Add gulp task to build languages map in Show language plugin (Fix [#671](https://github.com/PrismJS/prism/issues/671)) [[`39bd827`](https://github.com/PrismJS/prism/commit/39bd827)]
    _ Add reset styles to prevent bug in Coy theme ([#703](https://github.com/PrismJS/prism/issues/703)) [[`08dd500`](https://github.com/PrismJS/prism/commit/08dd500)]

### Other changes

-   Fixed link to David Peach article ([#647](https://github.com/PrismJS/prism/issues/647)) [[`3f679f8`](https://github.com/PrismJS/prism/commit/3f679f8)]
-   Added `complete` hook, which runs even when no grammar is found [[`e58b6c0`](https://github.com/PrismJS/prism/commit/e58b6c0), [`fd54995`](https://github.com/PrismJS/prism/commit/fd54995)]
-   Added test suite runner ([#588](https://github.com/PrismJS/prism/issues/588)) [[`956cd85`](https://github.com/PrismJS/prism/commit/956cd85)]
-   Added tests for every components
-   Added `.gitattributes` to prevent line ending changes in test files [[`45ca8c8`](https://github.com/PrismJS/prism/commit/45ca8c8)]
-   Split plugins into 3 columns on Download page [[`a88936a`](https://github.com/PrismJS/prism/commit/a88936a)]
-   Removed comment in components.js to make it easier to parse as JSON ([#679](https://github.com/PrismJS/prism/issues/679)) [[`2cb1326`](https://github.com/PrismJS/prism/commit/2cb1326)]
-   Updated README.md [[`1388256`](https://github.com/PrismJS/prism/commit/1388256)]
-   Updated documentation since the example was not relevant any more [[`80aedb2`](https://github.com/PrismJS/prism/commit/80aedb2)]
-   Fixed inline style for Coy theme [[`52829b3`](https://github.com/PrismJS/prism/commit/52829b3)]
-   Prevent errors in nodeJS ([#754](https://github.com/PrismJS/prism/issues/754)) [[`9f5c93c`](https://github.com/PrismJS/prism/commit/9f5c93c), [`0356c58`](https://github.com/PrismJS/prism/commit/0356c58)]
-   Explicitly make the Worker close itself after highlighting, so that users have control on this behaviour when directly using Prism inside a Worker. Fix [#492](https://github.com/PrismJS/prism/issues/492) [[`e42a228`](https://github.com/PrismJS/prism/commit/e42a228)]
-   Added some language aliases: js for javascript, xml, html, mathml and svg for markup [[`2f9fe1e`](https://github.com/PrismJS/prism/commit/2f9fe1e)]
-   Download page: Add a "Select all" checkbox ([#561](https://github.com/PrismJS/prism/issues/561)) [[`9a9020b`](https://github.com/PrismJS/prism/commit/9a9020b)]
-   Download page: Don't add semicolon unless needed in generated code. Fix [#273](https://github.com/PrismJS/prism/issues/273) [[`5a5eec5`](https://github.com/PrismJS/prism/commit/5a5eec5)]
-   Add language counter on homepage [[`889cda5`](https://github.com/PrismJS/prism/commit/889cda5)]
-   Improve performance by doing more work in the worker [[`1316abc`](https://github.com/PrismJS/prism/commit/1316abc)]
-   Replace Typeplate with SitePoint on homepage. Fix [#774](https://github.com/PrismJS/prism/issues/774) [[`0c54308`](https://github.com/PrismJS/prism/commit/0c54308)]
-   Added basic `.editorconfig` [[`c48f55d`](https://github.com/PrismJS/prism/commit/c48f55d)]

---

## 1.0.1 (2015-07-26)

### New components

-   **Brainfuck** ([#611](https://github.com/PrismJS/prism/issues/611)) [[`3ede718`](https://github.com/PrismJS/prism/commit/3ede718)]
-   **Keyman** ([#609](https://github.com/PrismJS/prism/issues/609)) [[`2698f82`](https://github.com/PrismJS/prism/commit/2698f82), [`e9936c6`](https://github.com/PrismJS/prism/commit/e9936c6)]
-   **Makefile** ([#610](https://github.com/PrismJS/prism/issues/610)) [[`3baa61c`](https://github.com/PrismJS/prism/commit/3baa61c)]
-   **Sass (Sass)** (fix [#199](https://github.com/PrismJS/prism/issues/199)) [[`b081804`](https://github.com/PrismJS/prism/commit/b081804)]
-   **VHDL** ([#595](https://github.com/PrismJS/prism/issues/595)) [[`43e6157`](https://github.com/PrismJS/prism/commit/43e6157)]

### Updated components

-   **ActionScript**:
    _ Fix ! operator and add ++ and -- as whole operators [[`6bf0794`](https://github.com/PrismJS/prism/commit/6bf0794)]
    _ Fix XML highlighting [[`90257b0`](https://github.com/PrismJS/prism/commit/90257b0)] \* Update examples to add inline XML [[`2c1626a`](https://github.com/PrismJS/prism/commit/2c1626a), [`3987711`](https://github.com/PrismJS/prism/commit/3987711)]
-   **Apache Configuration**: \* Don't include the spaces in directive-inline [[`e87efd8`](https://github.com/PrismJS/prism/commit/e87efd8)]
-   **AppleScript**:
    _ Allow one level of nesting in block comments [[`65894c5`](https://github.com/PrismJS/prism/commit/65894c5)]
    _ Removed duplicates between operators and keywords [[`1ec5a81`](https://github.com/PrismJS/prism/commit/1ec5a81)]
    _ Removed duplicates between keywords and classes [[`e8d09f6`](https://github.com/PrismJS/prism/commit/e8d09f6)]
    _ Move numbers up so they are not broken by operator pattern [[`66dac31`](https://github.com/PrismJS/prism/commit/66dac31)]
-   **ASP.NET**: \* Prevent Markup tags from breaking ASP tags + fix MasterType directive [[`1f0a336`](https://github.com/PrismJS/prism/commit/1f0a336)]
-   **AutoHotkey**:
    _ Allow tags (labels) to be highlighted at the end of the code [[`0a1fc4b`](https://github.com/PrismJS/prism/commit/0a1fc4b)]
    _ Match all operators + add comma to punctuation [[`f0ccb1b`](https://github.com/PrismJS/prism/commit/f0ccb1b)] \* Removed duplicates in keywords lists [[`fe0a068`](https://github.com/PrismJS/prism/commit/fe0a068)]
-   **Bash**:
    _ Simplify comment regex [[`2700981`](https://github.com/PrismJS/prism/commit/2700981)]
    _ Removed duplicates in keywords + removed unneeded parentheses [[`903b8a4`](https://github.com/PrismJS/prism/commit/903b8a4)]
-   **C**:
    _ Removed string pattern (inherited from C-like) [[`dcce1a7`](https://github.com/PrismJS/prism/commit/dcce1a7)]
    _ Better support for macro statements [[`4868635`](https://github.com/PrismJS/prism/commit/4868635)]
-   **C#**: \* Fix preprocessor pattern [[`86311f5`](https://github.com/PrismJS/prism/commit/86311f5)]
-   **C++**: \* Removed delete[] and new[] broken keywords [[`42fbeef`](https://github.com/PrismJS/prism/commit/42fbeef)]
-   **C-like**:
    _ Removed unused 'ignore' pattern [[`b6535dd`](https://github.com/PrismJS/prism/commit/b6535dd)]
    _ Use look-ahead instead of inside to match functions [[`d4194c9`](https://github.com/PrismJS/prism/commit/d4194c9)]
-   **CoffeeScript**: \* Prevent strings from ending with a backslash [[`cb6b824`](https://github.com/PrismJS/prism/commit/cb6b824)]
-   **CSS**:
    _ Highlight parentheses as punctuation [[`cd0273e`](https://github.com/PrismJS/prism/commit/cd0273e)]
    _ Improved highlighting of at-rules [[`e254088`](https://github.com/PrismJS/prism/commit/e254088)]
    _ Improved URL and strings [[`901812c`](https://github.com/PrismJS/prism/commit/901812c)]
    _ Selector regexp should not include last spaces before brace [[`f2e2718`](https://github.com/PrismJS/prism/commit/f2e2718)] \* Handle \r\n [[`15760e1`](https://github.com/PrismJS/prism/commit/15760e1)]
-   **Eiffel**: \* Fix string patterns order + fix /= operator [[`7d1b8d7`](https://github.com/PrismJS/prism/commit/7d1b8d7)]
-   **Erlang**: \* Fixed quoted functions, quoted atoms, variables and <= operator [[`fa286aa`](https://github.com/PrismJS/prism/commit/fa286aa)]
-   **Fortran**:
    _ Improved pattern for comments inside strings [[`40ae215`](https://github.com/PrismJS/prism/commit/40ae215)]
    _ Fixed order in keyword pattern [[`8a6d32d`](https://github.com/PrismJS/prism/commit/8a6d32d)]
-   **Handlebars**: \* Support blocks with dashes ([#587](https://github.com/PrismJS/prism/issues/587)) [[`f409b13`](https://github.com/PrismJS/prism/commit/f409b13)]
-   **JavaScript**:
    _ Added support for 'y' and 'u' ES6 JavaScript regex flags ([#596](https://github.com/PrismJS/prism/issues/596)) [[`5d99957`](https://github.com/PrismJS/prism/commit/5d99957)]
    _ Added support for missing ES6 keywords in JavaScript ([#596](https://github.com/PrismJS/prism/issues/596)) [[`ca68b87`](https://github.com/PrismJS/prism/commit/ca68b87)]
    _ Added `async` and `await` keywords ([#575](https://github.com/PrismJS/prism/issues/575)) [[`5458cec`](https://github.com/PrismJS/prism/commit/5458cec)]
    _ Added support for Template strings + interpolation [[`04f72b1`](https://github.com/PrismJS/prism/commit/04f72b1)]
    _ Added support for octal and binary numbers ([#597](https://github.com/PrismJS/prism/issues/597)) [[`a8aa058`](https://github.com/PrismJS/prism/commit/a8aa058)]
    _ Improve regex performance of C-like strings and JS regexps [[`476cbf4`](https://github.com/PrismJS/prism/commit/476cbf4)]
-   **Markup**:
    _ Allow non-ASCII chars in tag names and attributes (fix [#585](https://github.com/PrismJS/prism/issues/585)) [[`52fd55e`](https://github.com/PrismJS/prism/commit/52fd55e)]
    _ Optimized tag's regexp so that it stops crashing on large unclosed tags [[`75452ba`](https://github.com/PrismJS/prism/commit/75452ba)]
    _ Highlight single quotes in attr-value as punctuation [[`1ebcb8e`](https://github.com/PrismJS/prism/commit/1ebcb8e)]
    _ Doctype and prolog can be multi-line [[`c19a238`](https://github.com/PrismJS/prism/commit/c19a238)]
-   **Python**:
    _ Added highlighting for function declaration ([#601](https://github.com/PrismJS/prism/issues/601)) [[`a88aae8`](https://github.com/PrismJS/prism/commit/a88aae8)]
    _ Fixed wrong highlighting of variables named a, b, c... f ([#601](https://github.com/PrismJS/prism/issues/601)) [[`a88aae8`](https://github.com/PrismJS/prism/commit/a88aae8)]
-   **Ruby**: \* Added support for string interpolation [[`c36b123`](https://github.com/PrismJS/prism/commit/c36b123)]
-   **Scss**:
    _ Fixed media queries highlighting [[`bf8e032`](https://github.com/PrismJS/prism/commit/bf8e032)]
    _ Improved highlighting inside at-rules [[`eef4248`](https://github.com/PrismJS/prism/commit/eef4248)] \* Match placeholders inside selectors (fix [#238](https://github.com/PrismJS/prism/issues/238)) [[`4e42e26`](https://github.com/PrismJS/prism/commit/4e42e26)]
-   **Swift**: \* Update keywords list (fix [#625](https://github.com/PrismJS/prism/issues/625)) [[`88f44a7`](https://github.com/PrismJS/prism/commit/88f44a7)]

### Updated plugins

-   **File Highlight**: \* Allow to specify the highlighting language. Fix [#607](https://github.com/PrismJS/prism/issues/607) [[`8030db9`](https://github.com/PrismJS/prism/commit/8030db9)]
-   **Line Highlight**:
    _ Fixed incorrect height in IE9 ([#604](https://github.com/PrismJS/prism/issues/604)) [[`f1705eb`](https://github.com/PrismJS/prism/commit/f1705eb)]
    _ Prevent errors in IE8 [[`5f133c8`](https://github.com/PrismJS/prism/commit/5f133c8)]

### Other changes

-   Removed moot `version` property from `bower.json` ([#594](https://github.com/PrismJS/prism/issues/594)) [[`4693499`](https://github.com/PrismJS/prism/commit/4693499)]
-   Added repository to `bower.json` ([#600](https://github.com/PrismJS/prism/issues/600)) [[`8e5ebcc`](https://github.com/PrismJS/prism/commit/8e5ebcc)]
-   Added `.DS_Store` to `.gitignore` [[`1707e4e`](https://github.com/PrismJS/prism/commit/1707e4e)]
-   Improve test drive page usability. Fix [#591](https://github.com/PrismJS/prism/issues/591) [[`fe60858`](https://github.com/PrismJS/prism/commit/fe60858)]
-   Fixed prism-core and prism-file-highlight to prevent errors in IE8 [[`5f133c8`](https://github.com/PrismJS/prism/commit/5f133c8)]
-   Add Ubuntu Mono font to font stack [[`ed9d7e3`](https://github.com/PrismJS/prism/commit/ed9d7e3)]

---

## 1.0.0 (2015-05-23)

-   First release
-   Supported languages:
    _ ActionScript
    _ Apache Configuration
    _ AppleScript
    _ ASP.NET (C#)
    _ AutoHotkey
    _ Bash
    _ C
    _ C#
    _ C++
    _ C-like
    _ CoffeeScript
    _ CSS
    _ CSS Extras
    _ Dart
    _ Eiffel
    _ Erlang
    _ F#
    _ Fortran
    _ Gherkin
    _ Git
    _ Go
    _ Groovy
    _ Haml
    _ Handlebars
    _ Haskell
    _ HTTP
    _ Ini
    _ Jade
    _ Java
    _ JavaScript
    _ Julia
    _ LaTeX
    _ Less
    _ LOLCODE
    _ Markdown
    _ Markup
    _ MATLAB
    _ NASM
    _ NSIS
    _ Objective-C
    _ Pascal
    _ Perl
    _ PHP
    _ PHP Extras
    _ PowerShell
    _ Python
    _ R
    _ React JSX
    _ reST
    _ Rip
    _ Ruby
    _ Rust
    _ SAS
    _ Sass (Scss)
    _ Scala
    _ Scheme
    _ Smalltalk
    _ Smarty
    _ SQL
    _ Stylus
    _ Swift
    _ Twig
    _ TypeScript
    _ Wiki markup \* YAML
-   Plugins:
    _ Autolinker
    _ File Highlight
    _ Highlight Keywords
    _ Line Highlight
    _ Line Numbers
    _ Show Invisibles
    _ Show Language
    _ WebPlatform Docs
