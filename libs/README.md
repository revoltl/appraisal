# 编译依赖

以下三个 jar 被 `build.gradle` 以 `compileOnly` 引用，缺少时无法编译。

| 文件                                    | 用途                      |
| --------------------------------------- | ------------------------- |
| `ftb-ultimine-neoforge-2101.1.15.jar` | FTB Ultimine 连锁鉴定联动 |
| `architectury-13.0.11-neoforge.jar`   | FTB Ultimine 的前置       |
| `jei-1.21.1-neoforge-19.57.0.451.jar` | JEI 配方页                |

版本号变化时，同步修改 `build.gradle` 中对应的文件名。
