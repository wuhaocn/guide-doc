/**
 * ES6-Promises
 * https://github.com/jakearchibald/es6-promise
 */
!(function() {
	var a, b, c, d
	!(function() {
		var e = {},
			f = {}
		;(a = function(a, b, c) {
			e[a] = { deps: b, callback: c }
		}),
			(d = c = b = function(a) {
				function c(b) {
					if ('.' !== b.charAt(0)) return b
					for (
						var c = b.split('/'),
							d = a.split('/').slice(0, -1),
							e = 0,
							f = c.length;
						f > e;
						e++
					) {
						var g = c[e]
						if ('..' === g) d.pop()
						else {
							if ('.' === g) continue
							d.push(g)
						}
					}
					return d.join('/')
				}
				if (((d._eak_seen = e), f[a])) return f[a]
				if (((f[a] = {}), !e[a]))
					throw new Error('Could not find module ' + a)
				for (
					var g,
						h = e[a],
						i = h.deps,
						j = h.callback,
						k = [],
						l = 0,
						m = i.length;
					m > l;
					l++
				)
					'exports' === i[l] ? k.push((g = {})) : k.push(b(c(i[l])))
				var n = j.apply(this, k)
				return (f[a] = g || n)
			})
	})(),
		a('promise/all', ['./utils', 'exports'], function(a, b) {
			'use strict'
			function c(a) {
				var b = this
				if (!d(a)) throw new TypeError('You must pass an array to all.')
				return new b(function(b, c) {
					function d(a) {
						return function(b) {
							f(a, b)
						}
					}
					function f(a, c) {
						;(h[a] = c), 0 === --i && b(h)
					}
					var g,
						h = [],
						i = a.length
					0 === i && b([])
					for (var j = 0; j < a.length; j++)
						(g = a[j]), g && e(g.then) ? g.then(d(j), c) : f(j, g)
				})
			}
			var d = a.isArray,
				e = a.isFunction
			b.all = c
		}),
		a('promise/asap', ['exports'], function(a) {
			'use strict'
			function b() {
				return function() {
					process.nextTick(e)
				}
			}
			function c() {
				var a = 0,
					b = new i(e),
					c = document.createTextNode('')
				return (
					b.observe(c, { characterData: !0 }),
					function() {
						c.data = a = ++a % 2
					}
				)
			}
			function d() {
				return function() {
					j.setTimeout(e, 1)
				}
			}
			function e() {
				for (var a = 0; a < k.length; a++) {
					var b = k[a],
						c = b[0],
						d = b[1]
					c(d)
				}
				k = []
			}
			function f(a, b) {
				var c = k.push([a, b])
				1 === c && g()
			}
			var g,
				h = 'undefined' != typeof window ? window : {},
				i = h.MutationObserver || h.WebKitMutationObserver,
				j =
					'undefined' != typeof global
						? global
						: void 0 === this
							? window
							: this,
				k = []
			;(g =
				'undefined' != typeof process &&
				'[object process]' === {}.toString.call(process)
					? b()
					: i
						? c()
						: d()),
				(a.asap = f)
		}),
		a('promise/config', ['exports'], function(a) {
			'use strict'
			function b(a, b) {
				return 2 !== arguments.length ? c[a] : ((c[a] = b), void 0)
			}
			var c = { instrument: !1 }
			;(a.config = c), (a.configure = b)
		}),
		a('promise/polyfill', ['./promise', './utils', 'exports'], function(
			a,
			b,
			c
		) {
			'use strict'
			function d() {
				var a
				a =
					'undefined' != typeof global
						? global
						: 'undefined' != typeof window && window.document
							? window
							: self
				var b =
					'Promise' in a &&
					'resolve' in a.Promise &&
					'reject' in a.Promise &&
					'all' in a.Promise &&
					'race' in a.Promise &&
					(function() {
						var b
						return (
							new a.Promise(function(a) {
								b = a
							}),
							f(b)
						)
					})()
				b || (a.Promise = e)
			}
			var e = a.Promise,
				f = b.isFunction
			c.polyfill = d
		}),
		a(
			'promise/promise',
			[
				'./config',
				'./utils',
				'./all',
				'./race',
				'./resolve',
				'./reject',
				'./asap',
				'exports'
			],
			function(a, b, c, d, e, f, g, h) {
				'use strict'
				function i(a) {
					if (!v(a))
						throw new TypeError(
							'You must pass a resolver function as the first argument to the promise constructor'
						)
					if (!(this instanceof i))
						throw new TypeError(
							"Failed to construct 'Promise': Please use the 'new' operator, this object constructor cannot be called as a function."
						)
					;(this._subscribers = []), j(a, this)
				}
				function j(a, b) {
					function c(a) {
						o(b, a)
					}
					function d(a) {
						q(b, a)
					}
					try {
						a(c, d)
					} catch (e) {
						d(e)
					}
				}
				function k(a, b, c, d) {
					var e,
						f,
						g,
						h,
						i = v(c)
					if (i)
						try {
							;(e = c(d)), (g = !0)
						} catch (j) {
							;(h = !0), (f = j)
						}
					else (e = d), (g = !0)
					n(b, e) ||
						(i && g
							? o(b, e)
							: h
								? q(b, f)
								: a === D
									? o(b, e)
									: a === E && q(b, e))
				}
				function l(a, b, c, d) {
					var e = a._subscribers,
						f = e.length
					;(e[f] = b), (e[f + D] = c), (e[f + E] = d)
				}
				function m(a, b) {
					for (
						var c, d, e = a._subscribers, f = a._detail, g = 0;
						g < e.length;
						g += 3
					)
						(c = e[g]), (d = e[g + b]), k(b, c, d, f)
					a._subscribers = null
				}
				function n(a, b) {
					var c,
						d = null
					try {
						if (a === b)
							throw new TypeError(
								'A promises callback cannot return that same promise.'
							)
						if (u(b) && ((d = b.then), v(d)))
							return (
								d.call(
									b,
									function(d) {
										return c
											? !0
											: ((c = !0),
											  b !== d ? o(a, d) : p(a, d),
											  void 0)
									},
									function(b) {
										return c
											? !0
											: ((c = !0), q(a, b), void 0)
									}
								),
								!0
							)
					} catch (e) {
						return c ? !0 : (q(a, e), !0)
					}
					return !1
				}
				function o(a, b) {
					a === b ? p(a, b) : n(a, b) || p(a, b)
				}
				function p(a, b) {
					a._state === B &&
						((a._state = C), (a._detail = b), t.async(r, a))
				}
				function q(a, b) {
					a._state === B &&
						((a._state = C), (a._detail = b), t.async(s, a))
				}
				function r(a) {
					m(a, (a._state = D))
				}
				function s(a) {
					m(a, (a._state = E))
				}
				var t = a.config,
					u = (a.configure, b.objectOrFunction),
					v = b.isFunction,
					w = (b.now, c.all),
					x = d.race,
					y = e.resolve,
					z = f.reject,
					A = g.asap
				t.async = A
				var B = void 0,
					C = 0,
					D = 1,
					E = 2
				;(i.prototype = {
					constructor: i,
					_state: void 0,
					_detail: void 0,
					_subscribers: void 0,
					then: function(a, b) {
						var c = this,
							d = new this.constructor(function() {})
						if (this._state) {
							var e = arguments
							t.async(function() {
								k(c._state, d, e[c._state - 1], c._detail)
							})
						} else l(this, d, a, b)
						return d
					},
					catch: function(a) {
						return this.then(null, a)
					}
				}),
					(i.all = w),
					(i.race = x),
					(i.resolve = y),
					(i.reject = z),
					(h.Promise = i)
			}
		),
		a('promise/race', ['./utils', 'exports'], function(a, b) {
			'use strict'
			function c(a) {
				var b = this
				if (!d(a))
					throw new TypeError('You must pass an array to race.')
				return new b(function(b, c) {
					for (var d, e = 0; e < a.length; e++)
						(d = a[e]),
							d && 'function' == typeof d.then
								? d.then(b, c)
								: b(d)
				})
			}
			var d = a.isArray
			b.race = c
		}),
		a('promise/reject', ['exports'], function(a) {
			'use strict'
			function b(a) {
				var b = this
				return new b(function(b, c) {
					c(a)
				})
			}
			a.reject = b
		}),
		a('promise/resolve', ['exports'], function(a) {
			'use strict'
			function b(a) {
				if (a && 'object' == typeof a && a.constructor === this)
					return a
				var b = this
				return new b(function(b) {
					b(a)
				})
			}
			a.resolve = b
		}),
		a('promise/utils', ['exports'], function(a) {
			'use strict'
			function b(a) {
				return c(a) || ('object' == typeof a && null !== a)
			}
			function c(a) {
				return 'function' == typeof a
			}
			function d(a) {
				return '[object Array]' === Object.prototype.toString.call(a)
			}
			var e =
				Date.now ||
				function() {
					return new Date().getTime()
				}
			;(a.objectOrFunction = b),
				(a.isFunction = c),
				(a.isArray = d),
				(a.now = e)
		}),
		b('promise/polyfill').polyfill()
})()
