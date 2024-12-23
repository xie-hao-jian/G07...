<template>
  <button
    :class="[
      'px-4 py-2 rounded',
      variant === 'primary' ? 'bg-blue-500 text-white' : 'bg-gray-200'
    ]"
    :disabled="loading"
  >
    <slot v-if="!loading">{{ text }}</slot>
    <span v-else>加载中...</span>
  </button>
</template>

<script setup>
defineProps({
  text: String,
  variant: {
    type: String,
    default: 'primary'
  },
  loading: Boolean
})
</script> 