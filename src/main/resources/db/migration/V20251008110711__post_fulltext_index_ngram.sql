ALTER TABLE post
DROP INDEX ft_post_title_content,
  ADD FULLTEXT INDEX ft_post_title_content (title, content) WITH PARSER ngram;
-- 한글은 ngram을 사용하는게 유리, ngram_token_size=2 이라 2글자부터 인덱싱