var nav = require('./nav.js')
var { LanguageNav, FrameNav, MiddlewareNav, BaseNav, SolutionNav } = nav

var utils = require('./utils.js')
var { genNav, getComponentSidebar, deepClone } = utils

module.exports = {
  title: '开发文档',
  description: '开发文档',
  base: '/',
  head: [
    [
      'link',
      {
        rel: 'icon',
        href: '/favicon.ico'
      }
    ]
  ],
  themeConfig: {
    repo: 'coral-cloud/coral-doc',
    docsRepo: 'coral-cloud/coral-doc',
    docsDir: 'docs',
    editLinks: false,
    sidebarDepth: 3,
    locales: {
      '/': {
        label: '简体中文',
        selectText: '选择语言',
        nav: [
          {
            text: '指南',
            link: '/zh/guide/'
          },
          {
            text: '编程语言',
            items: genNav(deepClone(LanguageNav), 'ZH')
          },
          {
            text: '技术框架',
            items: genNav(deepClone(FrameNav), 'ZH')
          },
          {
            text: '技术中间件',
            items: genNav(deepClone(MiddlewareNav), 'ZH')
          },
          {
            text: '开发基础',
            items: genNav(deepClone(BaseNav), 'ZH')
          },
          {
            text: '解决方案',
            items: genNav(deepClone(SolutionNav), 'ZH')
          }
        ],
        sidebar: {
          '/zh/guide/': [
            {
              title: '开发文档',
              collapsable: true,
              children: genEssentialsSidebar('/zh')
            }
            // ,
            // {
            //   title: '5G应用',
            //   collapsable: true,
            //   children: genAdvancedSidebar('/zh')
            // }
          ]
        }
      }
    }
  },
  locales: {
    '/': {
      lang: 'zh-CN',
      description: '5G生态导读'
    }
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@public': './public'
      }
    }
  },
  ga: 'UA-109340118-1'
}

function genEssentialsSidebar(type = '') {
  const mapArr = ['/xg/5gc.md', '/xg/device.md']
  return mapArr.map(i => {
    return type + i
  })
}

function genAdvancedSidebar(type = '') {
  const mapArr = ['/xgapp/maap.md', '/xgapp/rcs.md']
  return mapArr.map(i => {
    return type + i
  })
}
