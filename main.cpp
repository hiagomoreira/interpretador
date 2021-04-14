int main()
{
	int a = 4, b =0, j=0, i=0, aa=10, tt=0;
    float c = 3, d = 1;
    double e = 4;

    if(c < e){
        while (e >= 0){
            while (a > 0){
                while(c > 0)
                {
                    b = b + 1;
                    if(b != 1)
                    {
                        d = d - 1;
                    }
                    c = c - 1;
                }
                a = a - 2;
            }
            e = e - 1;
        }

    }else if (a < b){
        b = 100;
    }else{
        b = 150;

        if(c > 1){
            aa = 20;
            tt = 1;
        }
    }

    do{
       tt = tt + 1 * aa;
       aa = aa-1;
    }while(aa>0);

    int pp = 5;
    for(pp = pp; pp < 0; pp++){
        tt=21;
    }
    double kk = 189.4778;

    if(kk == 10){
        kk = 89.78;
    }else{
        for(int uu = 10; uu > 0; uu--){
            kk = kk - uu + 14;
        }
    }
	return 0;
}