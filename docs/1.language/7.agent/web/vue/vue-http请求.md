### Vue-Http 请求

    Vue.js使用-http请求
    Vue.js使用-ajax使用

#### 1.为什么要使用 ajax

    前面的例子，使用的是本地模拟数据，通过ajax请求服务器数据。

#### 2.使用 jquery 的 ajax 库示例

    new Vue({
        el: '#app',
        data: {
            searchQuery: '',
            columns: [{name: 'name', iskey: true}, {name: 'age'},{name: 'sex', dataSource:['Male', 'Female']}],
            peoples: []
        },
        ready: function () {
            this.getPeoples();
        },
        methods: {
            getPeoples: function () {
                var vm = this;
                $.ajax({
                    url: 'http://localhost:20000/my_test',
                    type: 'get',
                    dataType: 'json',
                    success: function (data) {
                        vm.$set('peoples', data.result);
                    },
                    error: function(xhr, errorType, error) {
                        alert('Ajax request error, errorType: ' + errorType +  ', error: ' + error)
                    }
                });
            }
        }
    })

#### 3.vue-resource 库

    vue是基于数据驱动的，不需要直接操作DOM，因此没有必要引入jquery
    vue提供了一款轻量的http请求库，vue-resource
    vue-resource除了提供http请求外，还提供了inteceptor拦截器功能，在访问前，访问后，做处理。

#### 4.vue-resource 语法-使用$http 对象

    // 基于全局Vue对象使用http
    Vue.http.get('/someUrl',[options]).then(successCallback, errorCallback);
    Vue.http.post('/someUrl',[body],[options]).then(successCallback, errorCallback);
    // 在一个Vue实例内使用$http
    this.$http.get('/someUrl',[options]).then(sucessCallback, errorCallback);
    this.$http.post('/someUrl',[body],[options]).then(successCallback, errorCallback);
    then方法的回调函数写法有两种，第一种是传统的函数写法，第二种是更简洁的Lambda表达式写法。

    //传统写法
    this.$http.get('/someUrl',[options]).then(function(response){
        //响应成功回调
    },function(response){
        //响应错误回调
    });

    //Lambda写法
    this.$http.get('someUrl',[options]).then((response)=>{
        //响应成功回调
    },(response)=>{
        //响应错误回调
    });

#### 5.vue-resource 示例

    <script src="js/vue-resource.js"></script>
    <script>
    new Vue({
            el: '#app',
            data: {
                searchQuery: '',
                columns: [{name: 'name', iskey: true}, {name: 'age'},{name: 'sex', dataSource:['Male', 'Female']}],
                peoples: []
            },
            ready: function () {
                this.getPeoples();
            },
            methods: {
                getPeoples: function () {
                    var vm = this;
                    vm.$http.get('http://localhost:20000/my_test').then(
                            function (data) {
                                var newdata = JSON.parse(data.body)
                                vm.$set('peoples', newdata.result)
                            }).catch(function (response) {
                                console.log(response)
                            })
                }
            }
        })
    </script>

#### 6.vue-resource 用法-使用$resource 对象

    除了使用$http对象访问http，还可以使用$resource对象来访问。
    resource服务默认提供以下几种action:
    get:{method: 'GET'},
    save:{method: 'POST'},
    query:{method: 'GET'},
    update:{method: 'PUT'},
    remove:{method: 'DELETE'},
    delete:{method: 'DELETE'},

    resource对象使用示例如下：

    new Vue({
            el: '#app',
            data: {
                searchQuery: '',
                columns: [{name: 'name', iskey: true}, {name: 'age'},{name: 'sex', dataSource:['Male', 'Female']}],
                peoples: []
            },
            ready: function () {
                this.getPeoples();
            },
            methods: {
                getPeoples: function () {
                    var vm = this;
                    var resource = this.$resource('http://localhost:20000/my_test')

                    resource.get().then(
                            function (data) {
                                var newdata = JSON.parse(data.body)
                                vm.$set('peoples', newdata.result)
                            }).catch(function (response) {
                                console.log(response)
                            })
                }
            }
        })

#### 7.拦截器 interceptor

    语法如下：

    Vue.http.interceptors.push(function(request, next){
        //...
        //请求发送前的处理逻辑
        //...
        next(function(response){
            //...
            //请求发送后的处理逻辑
            //...
            //根据请求的状态，response参数会返回给successCallback或errorCallback
            return response
        })

    })

#### 8.拦截器 interceptor 使用示例

    <div id="help">
            <loading v-show="showLoading"></loading>
        </div>
    <template id="loading-template">
                <div class="loading-overlay">
                    <div class="sk-three-bounce">
                        <div class="sk-child sk-bounce1"></div>
                        <div class="sk-child sk-bounce2"></div>
                        <div class="sk-child sk-bounce3"></div>
                    </div>
                </div>
        </template>
    <script>
    var help = new Vue({
            el: '#help',
            data: {
                showLoading: false
            },
            components: {
                'loading': {
                    template: '#loading-template'
                }
            }
        })

        Vue.http.interceptors.push(function(request, next){
            help.showLoading = true
            next(function (response) {
                help.showLoading = false
                return response
            })
        })
    </script>

#### 9.vue-resource 的优点

    vue-resource比jquery轻量，可以使用Vue.http或者Vue.resource处理HTTP请求，两者没有什么区别。
    另外可以是用interceptor在请求前和请求后附加一些行为。
