FROM openjdk:22-ea-21-jdk-slim-bullseye

# 以 root 用户运行以下命令
USER root

# 添加一个名为bigstock的用户
RUN useradd -m -s /bin/bash bigstock

# 授予bigstock用户sudo权限
RUN echo "bigstock ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

#RUN apt-get update && \
#    apt-get install -y maven && \
#    apt-get clean

# Workspace path
WORKDIR /home/bigstock

# 将bigstockmodule复制到bigstock文件夹下
COPY ./bigstock-schedule.jar /home/bigstock/
COPY ./init.sh /opt/bigstock/
# 执行init.sh脚本
CMD ["bash", "/opt/bigstock/init.sh"]