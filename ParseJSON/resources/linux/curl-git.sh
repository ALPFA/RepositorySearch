echo "How many pages information do you need?"
read pageCount
cd /home/theja/ && {
for ((i = 1 ;i <= $pageCount;i++))
do
	curl -u pvempa2@uic.edu:ADPadpmay28 -o Page$i.json "https://api.github.com/search/repositories?q=testing+language:java+NOT+android+NOT+javascript+NOT+jquery+NOT+ios+NOT+xml&sort=stars?&page=$i";

	if (( $i % 30 == 0 ))
	then
	    sleep 60;
	fi
done

cd -;
}
