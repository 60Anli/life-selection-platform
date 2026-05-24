Vue.component("support-widget", {
  template: `
    <div class="support-widget">
      <button class="support-fab" @click="open = true" title="智能客服">
        <i class="el-icon-chat-dot-round"></i>
      </button>
      <div v-if="open" class="support-mask" @click.self="open = false">
        <div class="support-panel">
          <div class="support-head">
            <div>
              <div class="support-title">智能客服</div>
              <div class="support-subtitle">在线 · 秒杀/商家/优惠券咨询</div>
            </div>
            <button class="support-close" @click="open = false">
              <i class="el-icon-close"></i>
            </button>
          </div>
          <div class="support-body" ref="body">
            <div class="support-msg assistant">
              <div class="support-bubble">你好，我是生活优选平台智能客服。你可以问我商家地址、优惠券、秒杀规则或订单排队问题。</div>
            </div>
            <div class="support-quick">
              <button v-for="s in suggestions" :key="s" @click="ask(s)">{{s}}</button>
            </div>
            <div v-for="m in messages" :key="m.id" class="support-msg" :class="m.role">
              <div class="support-bubble">{{m.content}}</div>
            </div>
          </div>
          <div class="support-input">
            <el-input
              v-model="message"
              size="small"
              placeholder="输入咨询内容"
              @keyup.enter.native="send"
              :disabled="loading">
            </el-input>
            <el-button type="primary" size="small" @click="send" :loading="loading">发送</el-button>
          </div>
        </div>
      </div>
    </div>
  `,
  data() {
    return {
      open: false,
      message: "",
      sessionId: sessionStorage.getItem("supportSessionId") || "",
      loading: false,
      counter: 0,
      messages: [],
      suggestions: ["查询商家 1", "查看店铺 1 的优惠券", "秒杀规则"]
    }
  },
  methods: {
    ask(text) {
      this.message = text;
      this.send();
    },
    send() {
      const text = this.message.trim();
      if (!text || this.loading) {
        return;
      }
      this.messages.push({id: ++this.counter, role: "user", content: text});
      this.message = "";
      this.loading = true;
      this.scrollToBottom();
      axios.post("/customer-service/chat", {
        sessionId: this.sessionId,
        message: text
      }).then(data => {
        this.sessionId = data.sessionId;
        sessionStorage.setItem("supportSessionId", this.sessionId);
        this.suggestions = data.suggestions || this.suggestions;
        this.messages.push({id: ++this.counter, role: "assistant", content: data.reply});
        this.scrollToBottom();
      }).catch(err => {
        this.$message.error(err);
      }).finally(() => {
        this.loading = false;
      });
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const body = this.$refs.body;
        if (body) {
          body.scrollTop = body.scrollHeight;
        }
      });
    }
  }
});

Vue.component("footBar", {
  template: `
    <div class="foot">
      <div class="foot-box" :class="{active: activeBtn === 1}" @click="toPage(1)">
        <div class="foot-view"><i class="el-icon-s-home"></i></div>
        <div class="foot-text">首页</div>
      </div>
      <div class="foot-box" :class="{active: activeBtn === 2}" @click="toPage(2)">
        <div class="foot-view"><i class="el-icon-map-location"></i></div>
        <div class="foot-text">地图</div>
      </div>
      <div class="foot-box" @click="toPage(0)">
        <img class="add-btn" src="/imgs/add.png" alt="">
      </div>
      <div class="foot-box" :class="{active: activeBtn === 4}" @click="toPage(4)">
        <div class="foot-view"><i class="el-icon-user"></i></div>
        <div class="foot-text">我的</div>
      </div>
    </div>
  `,
  props: ["activeBtn"],
  methods: {
    toPage(i) {
      if (i === 0) {
        location.href = "/blog-edit.html";
      } else if (i === 1) {
        location.href = "/";
      } else if (i === 4) {
        location.href = "/info.html";
      }
    }
  }
});

document.addEventListener("DOMContentLoaded", function () {
  if (document.getElementById("support-widget-root")) {
    return;
  }
  const root = document.createElement("div");
  root.id = "support-widget-root";
  document.body.appendChild(root);
  new Vue({
    el: "#support-widget-root",
    template: "<support-widget />"
  });
});
