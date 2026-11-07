-- 图书管理系统 建库建表脚本
-- 用法：mysql -u root -p < sql/init.sql

CREATE DATABASE IF NOT EXISTS library_system
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE library_system;

DROP TABLE IF EXISTS books;

CREATE TABLE books (
  id         INT PRIMARY KEY AUTO_INCREMENT,
  title      VARCHAR(200)  NOT NULL COMMENT '书名',
  author     VARCHAR(100)  COMMENT '作者',
  price      DECIMAL(10,2) DEFAULT 0.00 COMMENT '价格',
  stock      INT           DEFAULT 0 COMMENT '库存',
  created_at TIMESTAMP     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO books (title, author, price, stock) VALUES
  ('三体',        '刘慈欣',           23.00, 5),
  ('活着',        '余华',             20.50, 3),
  ('平凡的世界',  '路遥',             28.00, 4),
  ('百年孤独',    '加西亚·马尔克斯',  39.90, 2),
  ('白夜行',      '东野圭吾',         35.00, 6);

SELECT id, title, author, price, stock FROM books;
