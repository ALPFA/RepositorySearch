@echo off
set user=pvempa2@uic.edu
set password=ADPadpmay28
set pagelimit=30

FOR /l %%X IN (1,1,%pagelimit%) DO (
	curl -u %user%:%password% -o Page%%X.json "https://api.github.com/search/repositories?q=testing+language:java+NOT+android+NOT+javascript+NOT+jquery+NOT+ios+NOT+xml&sort=stars?&page=%%X"
)