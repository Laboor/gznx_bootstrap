<template>
  <div>
    <Uploader />
  </div>
</template>

<script>
import Uploader from '@/components/Uploader';

export default {
  components: {
    Uploader,
  },
  data() {
    return {
      list: []
    }
  },
  methods: {
  }
};
</script>

<style scoped></style>
