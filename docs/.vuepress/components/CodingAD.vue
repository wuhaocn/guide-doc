<template>
  <a
    href="https://github.com/wuhaocn"
    target="_blank"
    @click="clickCoding('readme')"
    style="display: block;"
  >
    <img src="https://avatars3.githubusercontent.com/u/13733913?s=460&v=4">
  </a>
</template>

<script>
export default {
  methods: {
    clickCoding(tag) {
      ga('send', 'click', 'e.coding', 'Action', tag)
    }
  }
}
</script>
