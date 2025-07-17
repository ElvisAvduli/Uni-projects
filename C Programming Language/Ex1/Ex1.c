#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int signup_user();
void get_receipt();
void payment();
int terminate();
int login_user();
int logout();
int exit_user();
int login_root();
void change_cost();
float calculate_cost();
int exit_root();

char name[20],surname[20],username[10],password[10],address[50],yn,cc[30];
int days,age,engine;
float cost,rc,asfalistra,u22=0.00052,u30=0.00043,o30=0.00028,discount,ppcc;

int main(int argc, char *argv[]) 
{
	char choice[5],option;                                                                 /*dhlwsh metavlhtwn*/
	int answer,allow,z,payed,exit_u=0,exit_r=0,lo=0,sign=0,flag=0,term=0;
	cost=-1;
	payed=0;
	do
	{
		do
		{
			exit_u=0,exit_r=0,lo=0,flag=0;
			do 
			{
				printf("Type 'user' for user or 'root' for administrator:");                               /*10-21: elegxos egyrotitas gia tin omada xrhstwn*/
				scanf(" %s",&choice);
				system("cls");
				if ((strcmp(choice, "user")!=0) && (strcmp(choice, "root")!=0) && (strcmp(choice, "0")!=0))
				{
					printf("Invalid option\n");
		    	}
		    	if (choice[0]=='0')
		    	{
		    		flag=1;
				}
			}
			while((strcmp(choice, "user")!=0)  && (strcmp(choice, "root")!=0) && (flag!=1));
			switch(choice[0])
			{
				case ('u'):                                                      /*perpitwsh user*/
				{
					do
					{
						do
						{
						
							printf("1. Sign up \n2. Login \n3. Logout\nSelect your option:");                 /*26-43: Elegxos egyrotitas eisodou gia tin energeia pou theli na kanei o user*/
							scanf("%d",&answer);
							system("cls");
							if ((answer<1) || (answer>3))
							{
								printf("Invalid answer\n");	
							}
						}
						while((answer<=0) || (answer>3));       
						                          
						if (answer==1)                                                   /*periptwsh sign up*/
						{
							sign=signup_user();	                                            /*klhsh sinartisis signup_user*/
						}
						else if (answer==2)                                               /*periptwsh log in*/
						{
						if (sign==0)
							{
								printf("You have not signed up yet\n");
							}
							else
							{
								allow=login_user();                                         /*klhsh sinartisis login_user*/ 
								if (allow==1)                                                
								{
									system("cls");
									do
									{
						  				printf("a. Calculate Cost \nb. Payment \nc. Reciept \nd. Exit\nSelect your answer:");     /*menu epilogwn*/
								  		scanf(" %c",&option);
								  		system("cls");
										if (option=='a')                                                     /*periptwsh calculate cost*/
								  		{
								  			cost=calculate_cost();                                           /*klhsh sinartisis claculate_cost*/
											system("cls");
											printf("The cost is %.2f\n",cost);
										}
										else if (option=='b')                                /*periptwsh payment*/
										{
											if (cost!=-1)                                   /*an exei ipologistei to cost pio prin*/
											{
												payment();                              /*klisi sinartisis payment*/
										        payed=1;
											}
										    else                                            
									   		{
										    	printf("The cost has not been calculated\n"); 
											}											
										}
										else if(option=='c')                                 /*periptwsh receipt*/
										{
											if (payed==1)
											{
												get_receipt();                              /*klisi sinartisis get_receipt*/
											}
											else
											{
												printf("You have not payed yet\n");
											}
										}
			 	                	    else if (option=='d')
										{
											exit_u=exit_user();                                    /*klisi sinartisis exit_user*/
										}
									}
									while (exit_u!=1);
								}
							}
						}
						else                                                          /* periptwsh logout*/
						{
							lo=logout();                                                 /*klisi sinartisis logout*/
						}
					}
					while (lo!=1);
					break ;
					system("cls");
				}
				case('r'): 
				{
					system("cls");
					
					
						printf("1. Login\n2. Logout\nSelect your answer:");                               /*epiloges administrator*/
						scanf("%d", &answer);
						system("cls");
						if (answer==1)                                               /*periptwsh login*/
						{
							z=login_root();                                           /*klisi sinartisis login_root*/
							if (z==1)
							{
								do
								{
									system("cls");
									printf("a. Change Cost\nb. Exit\nSelect your answer:");                    /* epiloges administrator afou kanei login*/
									scanf(" %c", &option); 
									system("cls");      
									if (option=='a')                                      /*periptwsh change cost*/
									{
										change_cost();                                    /*klisi sinartisis change_cost*/
									}
									else                                                  /*periptwsh exit*/
									{
										exit_r=exit_root();                                      /*klisi sinartisis exit_root*/
									}
								}
								while (exit_r!=1);
							}
						}
						else                                                           /*periptwsh logout*/
						{                                                 
							lo=logout();                                                   /*klisi sinartisis logout*/
						}
					break;
				}
			}
			if (flag==1)
			{
				lo=logout();
				term=terminate();
			}
		}
		while (lo!=1);
	system("cls");
	}
	while (term!=1);                                                       /*h ektelesh tou programmatos tha sinexistei mexri o xrhsths na pathsei '0'*/
    return 0;
}


int signup_user()                                                           /*sinartisi signup_user*/
{
	char pas[50];                      
	system("cls"); 
	int lengthps;
	printf("Name:");
	scanf(" %s",&name);
	printf("Surname:");                                                       /*zhtountai ta stoixeia toy xrhsth*/
	scanf(" %s",&surname);
	printf("Address:");
	scanf(" %s",&address);
	printf("Username:");
	scanf(" %s",&username);
	do
	{
		printf("Password:");
		scanf(" %s", &pas);
		lengthps= strlen(pas);                                               /*zhteitai to password se loop oste na einai 6 xaraktires*/
		
    	if (lengthps!=6)
   	 	{
    		printf("Your password must contain 6 characters\n");
		}
	}
	while (lengthps!=6);
	strcpy(password,pas);                                                    /*antigrafetai to password stin global metavlhth*/
	system("cls");
	printf("You have signed up successfully\n");
	return 1;
}

int login_user()                                                             /*sinarthsh login_user*/
{
	char usrnm[10],pswrd[10];                                           
	do
	{	
		printf("Username:");
		scanf("%s", usrnm);                                                 /*zhtountai ta stoixeia tou user mexri na pliktrologhsei ta swsta stoixeia*/
		printf("Password:");
		scanf("%s", pswrd);
		system("cls");
		if (strcmp(username, usrnm) || strcmp(pswrd,password))
		{
			printf("Invalid username or password\n");
		}
	}
	while (strcmp(username, usrnm) || strcmp(pswrd,password));
	return 1;
}


float calculate_cost()
{
	printf("How old are you?\nAnswer:");                                        /*zhteitai h hlikia tou xrhsth*/
	scanf("%d", &age);
	printf("Desired cc:");               /*zhteitai posa kivika zhtaei o xrhsths to amaksi*/
	scanf("%d", &engine);
	if (age<=22)                                                       /*periptwsh age=18-22*/
	{
		asfalistra=age*u22*engine;
		ppcc=u22;
	}
	else if (age<=30)                                                  /*periptwsh age=22-30*/
	{
		asfalistra=age*u30*engine;
		ppcc=u30;
	}
	else                                                               /*periptwsh age=31+*/
	{
		asfalistra=age*o30*engine;
		ppcc=o30;
	}
	
	if (engine<=1600)                                                 /*periptwsh engine<=1600cc*/
	{
		rc=11.42*1;
	}
	else if (engine<=2000)                                            /*periptwsh engine<=2000cc*/
	{
		rc=13.82*1;
	}
	else                                                              /*periptwsh engine>2000*/
	{
		rc=16.22*1;
	}
	cost=asfalistra+rc;
	cost= round(cost*100)/100; 
	return cost;
}




void payment()                                                    /*sinartisi payment*/
{
	float amount;
	int answer,count,lengthcc;
	system("cls");
	do
	{
		printf("Would you like to pay with:\n1.Cash\n2.Credit Card\nPlace your answer:");      /*menu epilogwn gia tropo plirwmhs*/
		scanf("%d",&answer);
		system("cls");
	}
	while ((answer==1)&&(answer==2));
	if (answer==1)                                                          /*periptwsh cash*/
	{
		yn='n';
		discount=0;
		do
		{
			printf("Type the final cost:");                                 /*zhteitai o xrhsths na grapsei to swsto poso*/
			scanf("%f", &amount);
			if (cost != amount)				
			{
	   			printf("You have not typed the correct final cost\n");
			}			
		}
		while (amount!=cost);
		discount=0;
	}
	else                                                                   /*periptwsh credit card*/
	{
		yn='y';
		do
		{
			printf("Type your credit card number:");                        /*zhteitai o 16psifios arithmos credit card*/
			scanf("%s", &cc);
			lengthcc=strlen(cc);
			if (lengthcc!=16)
			{
		   		printf("Credit card number is a 16 digit number\n");
			}
		}
		while (lengthcc!=16);
		do
		{
			printf("Type the final cost:");                                 /*zhteitai o xrhsths na grapsei to swsto poso*/                 
			scanf("%f", &amount);
		  	if (cost!=amount)
		   	{
				printf("Credit card number is a 16 digit number\n");
			}
		}
		while (cost!=amount);
		discount=0.15*cost;                                                /*ipologismos discount */
		cost=cost-discount;                                                /*ipologismos telikou kostous*/
	}
    system("cls");
return;	
}



void get_receipt()                                                                     /*sinartisi get_receipt*/
{
	system("cls");
	printf("*************************************************************\n");
	printf("*                      CarRental S.A.                       *\n");
	printf("*                                                           *\n");
	printf("*                                                           *\n");
	printf("*    - Days:                  1     (11.42 EUR/ day)        *\n");
	printf("*    - Driver Age:            %d     (%.5f / CC)         *\n",age,ppcc);
	printf("*    - Engine CC:             %d                          *\n",engine);
	printf("*                                                           *\n");
	printf("*                                                           *\n");
	printf("*                      PAYMENT DETAILS                      *\n");
	printf("*                                                           *\n");
	printf("*    - AMOUNT:                %.2f EUR                     *\n",cost+discount);
	printf("*    - Pay with credit card?        %c                       *\n",yn);
	if (discount<10)
	{
		
	printf("*    - Discount(15%):         %.2f EUR                       *\n",discount);
	}
	else
	{
	printf("*    - Discount(15%):         %.2f EUR                      *\n",discount);	
	}
	printf("*    - TOTAL AMOUNT:          %.2f EUR                     *\n",(cost));	
	printf("*                                                           *\n");
	printf("*                                                           *\n");
	printf("*                       CREDIT CARD                         *\n");	
	printf("*                                                           *\n");
	printf("*    - Credit card number:                                  *\n");               
	if (yn=='y')	                                                                           /*gia na emfanizontai se seira ta * */
	{
		printf("*             %s                              *\n",cc);
	}
	else
	{
		printf("*                                                           *\n");
	}
	printf("*                                                           *\n");
	printf("*                                                           *\n");	
    printf("*************************************************************\n");
    return;
}
    
int login_root()                                                              /*sinartisi login_root*/
{
	char usrnm[4],ps[7];                                                      /*dhlwsh metavlhtwn*/
	do
	{
		printf("Username:");
		scanf("%s",usrnm);
		printf("Password:");
		scanf("%s",ps);	
		system("cls");
	   if ((strcmp(usrnm,"root"))&& (strcmp(ps,"Root123")))                  /*elegxos egirotitas oste o administrator na pliktroloisei swsto username kai password*/
        {
        	printf("Invalid username or password. Try again\n");
		}
	}
	while ((strcmp(usrnm,"root"))&& (strcmp(ps,"Root123")));

	return 1;
}


void change_cost()                                  /*sinaritisi change_cost*/ 
{
	float c;
	int vathmida;
	printf("Choose the price of the category you want to change (1,2,3)\nPlace your answer:");                         /*epilegei o xristis mia vathmida*/
	scanf("%d",&vathmida);
	if (vathmida==1)                                /*periptwsh age= 18-22*/
	{
		do                                            
		{
			printf("You would like to change %.5f to: ",u22);
			scanf("%f",&c);
			if ((c<0.9*u22) ||(c>1.1*u22))          /*an o xristis theli na allaksei tin timi tou  10% parapanw i ligotero apo tin idi iparxousa timi*/
			{
				printf("Try again\n");
			}
		}
		while((c<0.9*u22) || (c>1.1*u22));
		u22=c;
	}
	else if (vathmida==2)                           /*periptwsh age=23-30*/
	{
		do
		{
			printf("You would like to change %.5f to: ",u30);
			scanf("%f",&c);
			if ((c<0.9*u30) ||(c>1.1*u30))          /*an o xristis theli na allaksei tin timi tou  10% parapanw i ligotero apo tin idi iparxousa timi*/
			{
				printf("Try again\n");
			}
		}
		while((c<0.9*u30) || (c>1.1*u30));
		u30=c;
	}
	else                                            /*periptwsh age=30+*/
	{	
		do
		{
			printf("You would like to change %.5f to: ",o30);
			scanf("%f",&c);
			if ((c<0.9*o30) ||(c>1.1*o30))          /*an o xristis theli na allaksei tin timi tou  10% parapanw i ligotero apo tin idi iparxousa timi*/
			{
				printf("Try again \n");
			}
		}
		while((c<0.9*o30) || (c>1.1*o30));
		o30=c;
	}
return ;	
}

int exit_user()                                  /*sinartisi exit_user*/
{
	return 1;                                    
}

int exit_root()                                  /*sinartisi exit_root*/
{
	return 1;
}

int terminate()                                 /*sinartisi terminate*/
{
	return 1;
}

int logout()                                    /*sinartisi logout*/
{
	return 1;
}
