package cloud.waldiekiste.java.projekte.cloudnet.webinterface.http.v2;

import cloud.waldiekiste.java.projekte.cloudnet.webinterface.ProjectMain;
import cloud.waldiekiste.java.projekte.cloudnet.webinterface.http.v2.utils.HttpUtil;
import cloud.waldiekiste.java.projekte.cloudnet.webinterface.http.v2.utils.JsonUtil;
import cloud.waldiekiste.java.projekte.cloudnet.webinterface.http.v2.utils.RequestUtil;
import cloud.waldiekiste.java.projekte.cloudnet.webinterface.http.v2.utils.ResponseUtil;
import cloud.waldiekiste.java.projekte.cloudnet.webinterface.http.v2.utils.UserUtil;
import de.dytanic.cloudnet.lib.serverselectors.sign.Sign;
import de.dytanic.cloudnet.lib.serverselectors.sign.SignLayoutConfig;
import de.dytanic.cloudnet.lib.user.User;
import de.dytanic.cloudnet.lib.utility.document.Document;
import de.dytanic.cloudnet.web.server.handler.MethodWebHandlerAdapter;
import de.dytanic.cloudnet.web.server.util.PathProvider;
import de.dytanic.cloudnet.web.server.util.QueryDecoder;
import de.dytanic.cloudnetcore.CloudNet;
import de.dytanic.cloudnetcore.network.components.MinecraftServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SignApi extends MethodWebHandlerAdapter {

  private final Path path;
  private final ProjectMain projectMain;

  /**
   * Process the request about the sign system for cloudnet.
   * @param projectMain The main class from the project
   */
  public SignApi(ProjectMain projectMain) {
    super("/cloudnet/api/v2/sign");
    CloudNet.getInstance().getWebServer().getWebServerProvider().registerHandler(this);
    this.path = Paths.get("local/signLayout.json");
    this.projectMain = projectMain;
  }

  @SuppressWarnings("deprecation")
  @Override
  public FullHttpResponse get(ChannelHandlerContext channelHandlerContext,
      QueryDecoder queryDecoder,
      PathProvider pathProvider, HttpRequest httpRequest) {
    FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
        httpRequest.getProtocolVersion(),
        HttpResponseStatus.OK);
    fullHttpResponse = HttpUtil.simpleCheck(fullHttpResponse, httpRequest);
    User user = HttpUtil.getUser(httpRequest);

    if (!UserUtil.hasPermission(user, "*", "cloudnet.web.module.sign.load")) {
      return ResponseUtil.success(fullHttpResponse, false, new Document());
    }
    Document resp = new Document();
    switch (RequestUtil.getHeaderValue(httpRequest, "-Xmessage").toLowerCase(Locale.ENGLISH)) {
      case "check":
        resp.append("response", !CloudNet.getInstance().getConfig().getDisabledModules()
            .contains("CloudNet-Service-SignsModule"));
        return ResponseUtil.success(fullHttpResponse, true, resp);
      case "config":
        Document document = Document.loadDocument(this.path);
        SignLayoutConfig signLayoutConfig = JsonUtil.getGson()
            .fromJson(document.get("layout_config"),SignLayoutConfig.class);
        resp.append("response", JsonUtil.getGson().toJson(signLayoutConfig));
        return ResponseUtil.success(fullHttpResponse, true, resp);
      case "random":
        Random random = new Random();
        ArrayList<MinecraftServer> arrayList = new ArrayList<>(
            CloudNet.getInstance().getServers().values());
        if (arrayList.size() > 0) {
          resp.append("response",
              JsonUtil.getGson().toJson(arrayList.get(random.nextInt(arrayList.size()))));
          return ResponseUtil.success(fullHttpResponse, true, resp);
        } else {
          return ResponseUtil.success(fullHttpResponse, false, new Document());
        }
      case "db":
        resp.append("response",
            projectMain.getSignDatabase().loadAll().values().stream().map(sign ->
                JsonUtil.getGson().toJson(sign)).collect(Collectors.toList()));
        return ResponseUtil.success(fullHttpResponse, true, resp);
      default:
        return ResponseUtil.messageFieldNotFound(fullHttpResponse);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public FullHttpResponse post(ChannelHandlerContext channelHandlerContext,
      QueryDecoder queryDecoder,
      PathProvider pathProvider, HttpRequest httpRequest) {
    FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
        httpRequest.getProtocolVersion(),
        HttpResponseStatus.OK);
    fullHttpResponse = HttpUtil.simpleCheck(fullHttpResponse, httpRequest);
    User user = HttpUtil.getUser(httpRequest);
    switch (RequestUtil.getHeaderValue(httpRequest, "-Xmessage").toLowerCase(Locale.ENGLISH)) {
      case "save": {
        String content = RequestUtil.getContent(httpRequest);
        if (content.isEmpty()) {
          return ResponseUtil.success(fullHttpResponse, false, new Document());
        }
        if (!UserUtil.hasPermission(user, "*", "cloudnet.web.module.sign.save")) {
          return ResponseUtil.success(fullHttpResponse, false, new Document());
        }
        SignLayoutConfig signLayoutConfig = JsonUtil.getGson()
            .fromJson(content, SignLayoutConfig.class);
        final Document document = Document.loadDocument(this.path);
        document.append("layout_config", signLayoutConfig);
        document.saveAsConfig(this.path);
        CloudNet.getInstance().getNetworkManager().updateAll();
        return ResponseUtil.success(fullHttpResponse, true, new Document());
      }
      case "delete": {
        String content = RequestUtil.getContent(httpRequest);
        if (!UserUtil.hasPermission(user, "*", "cloudnet.web.module.sign.delete.*")) {
          return ResponseUtil.success(fullHttpResponse, false, new Document());
        }
        UUID id = UUID.fromString(content);
        projectMain.getSignDatabase().removeSign(id);
        CloudNet.getInstance().getNetworkManager().updateAll();
        return ResponseUtil.success(fullHttpResponse, true, new Document());
      }

      case "add": {
        if (!UserUtil.hasPermission(user, "*", "cloudnet.web.module.sign.add")) {
          return ResponseUtil.success(fullHttpResponse, false, new Document());
        }
        String content = RequestUtil.getContent(httpRequest);
        Sign s = JsonUtil.getGson().fromJson(content, Sign.class);
        projectMain.getSignDatabase().appendSign(s);
        CloudNet.getInstance().getNetworkManager().updateAll();
        return ResponseUtil.success(fullHttpResponse, true, new Document());
      }
      default: {
        return ResponseUtil.messageFieldNotFound(fullHttpResponse);
      }
    }
  }

  @Override
  public FullHttpResponse options(ChannelHandlerContext channelHandlerContext,
      QueryDecoder queryDecoder,
      PathProvider pathProvider, HttpRequest httpRequest) {
    return ResponseUtil.cross(httpRequest);
  }
}