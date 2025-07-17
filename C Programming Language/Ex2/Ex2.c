#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

/* run this program using the console pauser or add your own getch, system("pause") or input loop */

struct Flight_Destinations																						/*dhlwsh domhs Flight Destinations gia oles tis topothesies pu mporei na paei kapoios*/
{
char AP[3];
int FA;
int	KP;
}p[10];

struct Person																									/*dhlwsh domhs Person*/
{
	char name[20];
	char surname[20];
	struct Address																								/*emfolevmeni domh address*/
	{
		char road[20];
		int number;
		char area[20];
		int TK;
	}address[20];
	char username[20];
	char password[20];
	char flight[3][4][10];
}person[20];

void login_admin();																								/*dhlwsh sinartisewn*/
void set_prices();
void signup_user();
void login_user();
void view_user();
void modify_user();
void calculate_cost();
void payment();
void most_expensive();


int pos,plithos=-1,payed[20][2];																		   		/*dhlwsh global metavlithw gia prosdiorismo user mesa sto programma*/
int main(int argc, char *argv[])
{
	int	payed[20][2],i,j,k=0,date;														   						/*dhlwsh metavlhtwn*/
	char choice[5];
	int f2=0,f3=0,lo,lo2=0,answer,answer2;
	
	for(i=0;i<=20;i++)
	{
		for(j=0;j<=2;j++)
		{
			payed[i][j]=0;																						/*arxikopoihsh pinaka payed giati kaneis den exei plhrwsei kamia pthsh*/
		}
	}			
	do
	{
		lo=0;																									/*arxikopoihsh mesa stin do */
	
		do
		{
			do 
			{
				printf("Type 'user' for user, 'admin' for administrator or '0' for exit:");                     /*10-21: elegxos egyrotitas gia tin omada xrhstwn*/
				scanf(" %s",&choice);
				system("cls");
				if ((strcmp(choice, "user")!=0) && (strcmp(choice, "admin")!=0) && (strcmp(choice, "0")!=0))
				{
					printf("Invalid option\n");
				}
			}
			while((strcmp(choice, "user")!=0)  && (strcmp(choice, "admin")!=0) && (strcmp(choice, "0")!=0));
			system("cls");
			if (strcmp(choice, "0")==0)																			/*periptwsh 0 gia exit*/
			{
				return 0;
			}
			if (((f2==1)||(strcmp(choice, "admin")!=0))&&(f2==0))
			 	{
				 printf("Te administrator has not set flight destinations and prices yet\n");	
				}											
		}
		while (((f2==1)||(strcmp(choice, "admin")!=0))&&(f2==0));
		switch(choice[0])
		{
			case 'a':																							/*periptwsh admin*/
			{	
				system("cls");
				login_admin();																					/*klisi sinartisis login_admin*/
				do
				{
					do
					{
				 		printf("1. Set Prices\n2. Log Out\nSelect your answer:");								/*menu admin*/
				 		scanf("%d",&answer);
				 		system("cls");
				 		if ((answer<1)||(answer>2))																/*elegxos egirotitas*/
				 		{
				 			printf("Invalid Answer\n");
				 		}
					 }
				 	while ((answer<1)||(answer>2));
				 	system("cls");
					if (answer==1)																				/*periptwsh setprices*/
					{
						set_prices();																			/*klhsh synarthshs set_prices*/
						f2=1;
					}
					else																						/*periptwsh logout*/
					{
						lo=1;
					}
				}
				while (lo==0);	
				break;																							/*eksodos apo tin othoni tou admin*/
			}
			default:																							/*periptwsh user*/
			{
				f3=0;
				do
				{
					lo2=0;
					do
					{
						printf("1. Register\n2. Login\n3. Logout\nPlace your answer:");							/*menu user*/
						scanf("%d",&answer);
						system("cls");
						if((answer<1)||(answer>3))																/*elegxos egirotitas*/
						{
							printf("Invalid Answer\n");
						}
					}
					while ((answer<1)||(answer>3));																	
					if (answer==1)																				/*perpitwsh Register*/
					{
						signup_user();																			/*klhsh synarthshs signup_user*/
					}
					else if (answer==2)																			/*periptwsh Login*/
					{
						login_user();																			/*klhsh sinarthshs login_user*/														
						sleep(5);																				/*minima pou tha emfanizetai gia 5 defterolepta*/
						system("cls");
						do
						{
							lo=0;
							do	
							{
								printf("INDIVIDUAL CLIENT'S FUNCTIONS\n\n");									/*othoni epimerous leiturgiwn xrhsth*/
								printf("1. View\n2. Modify\n3. Calculate\n4. Payment\n5. Most expensive\n6.Logout\nPlace your answer:");		/*menu sindedemenou user*/
								scanf("%d",&answer2);
								if ((answer2<1)||(answer2>6));													/*elegxos egyrothtas*/
								{
									printf("Invalid Answer\n");
								}
							}
							while ((answer2<1)||(answer2>6));
							system("cls");
							switch(answer2)
							{
								case 1:																			/*periptwsh View*/
								{
									view_user();																/*klisi sinartishs view_user*/
									break;							
								}
								case 2:																			/*perpitwsh Modify*/	
								{
									modify_user();																/*klhsh sinartishs modify_user*/
									break;
								}
								case 3:																			/*periptwsh calsculate*/
								{
									calculate_cost();															/*klhsh sinartishs calculate*/
									f3=1;
									break;
								}
								case 4:																			/*periptwsh payment*/
								{
									if (f3>=1)
									{
										payment();																/*klhsh sinartishw payment*/
										f3=2;
									}
									else
									{
										printf("You have not calculated the cost of your flights yet\n");
									}
									break;
								}
								case 5:																			/*periptwsh most expensive*/
								{
									if (f3==2)
									{
										most_expensive();														/*klhsh sinartishs most_expensive*/
									}
									else
									{
										printf("You have not payed any flight yet\n");
									}
									break;
								}
								default:																		/*perpitwsh logout*/
								{
									lo=1;															
									break;
								}
							}
						}
						while (lo==0);																			/*eksodos apo tin othoni epimerous leiturgiwn xrhsth*/
					}
					else
					{
						lo2=1;
					}
				}
				while (lo2==0);																					/*eksodos apo tin othoni tou user*/
				break;	
			}
		}
	}
	while (lo2!=4);	
	printf("exit");																								/*to programma tha epistrefei sthn arxh gia panta mexri o xrhsthw na pathsei 0*/
	return 0;
}



void login_admin()																								/*sinartish login_admin*/
{
	char usrnm[4],ps[7];                                                    									/*dhlwsh metavlhtwn*/
	do
	{
		printf("Username:");
		scanf("%s",usrnm);
		printf("Password:");
		scanf("%s",ps);	
		system("cls");
	   if ((strcmp(usrnm,"airadmin")!=0)|| (strcmp(ps,"Air123")!=0))                 							/*elegxos egirotitas oste o administrator na pliktroloisei swsto username kai password*/
        {
        	printf("Invalid username or password. Try again\n");
		}
	}
	while ((strcmp(usrnm,"airadmin")!=0)|| (strcmp(ps,"Air123")!=0));
}



void set_prices()																								/*sinarthsh set_prices*/
{
	int i;
  	for(i=0;i<10;i++)																							/*vrogxos gia 10 proorismous*/
  	{
    	printf( "Type the first three numbers of the destination No.%d:",i+1);
    	scanf("%s", &p[i].AP);
    	printf("Type the airport taxes for the destination No.%d:",i+1);
    	scanf("%d", &p[i].FA);
    	printf("Type the flight cost for destination No.%d:",i+1);
    	scanf("%d",&p[i].KP);
    	system("cls");
	}
}


void signup_user()																								/*sinarthsh signup_user*/
{
	system("cls");
	if (plithos==19)
	{
		printf("Unable to sign up\n");
	}
	else
	{
		plithos++;
		printf("Name:");
		scanf("%s", &person[plithos].name);
		printf("Surname:");																						/*zhtountai ta stoixeia tou xrhsth */
		scanf("%s", &person[plithos].surname);
		printf("Road:");
		scanf("%s", &person[plithos].address[plithos].road);
		printf("Number:");
		scanf("%d", &person[plithos].address[plithos].number);
		printf("Area:");
		scanf("%s", &person[plithos].address[plithos].area);
		printf("TK:");
		scanf("%d", &person[plithos].address[plithos].TK);
		strcpy(person[plithos].username, strcat(person[plithos].name,"456"));									/*dhmiourgia username */
		printf("Password:");
		scanf("%s", &person[plithos].password);
		system("cls");
	}															
}


void login_user()																								/*sinarthsh login_user*/
{
	char usrnm[20],ps[20];
	int flag=0;
	int i;
	do
	{
		printf("Username:");
		scanf("%s", usrnm);     	                                            								/*zhtountai ta stoixeia tou user mexri na pliktrologhsei ta swsta stoixeia*/
		printf("Password:");	
		scanf("%s", ps);
		system("cls");
		i=0;
		while ((flag==0)&&(i<=plithos))																			/*anazhthsh gia atomo*/	
		{
			if (((strcmp(person[i].username,usrnm)) || (strcmp(person[i].password,ps)))==0)						/*an yparxei*/
			{
				flag=1;
				printf("YOU HAVE SUCCESSFULLY LOGGED IN\n");
				pos=i;
			}
			i++;
		}
		if (flag==0)																							/*an den yparxei*/
		{
			printf("User not found. Please try again\n");
		}
	}
	 while (flag==0);
}


void view_user()																								/*sinarthsh view_user*/
{
	system("cls");
	printf(" Name: %s\n", person[pos].name);
	printf(" Surname: %s\n", person[pos].surname);																/*emfanish stoixewn user*/
	printf(" Address: %s %d, %s\n", person[pos].address[pos].road, person[pos].address[pos].number, person[pos].address[pos].area);
	printf(" TK: %d\n", person[pos].address[pos].TK);
	printf(" Username: %s\n", person[pos].username);
	printf(" Password: %s\n", person[pos].password);

}


void modify_user()																								/*sinartisi modify_user*/
{
	system("cls");
	printf("Enter new data:\n");																				/*epitrepetai h allagi mono twn stoixewn tou address*/
	printf("Road:");
	scanf("%s", &person[pos].address[pos].road);
	printf("Number:");
	scanf("%d", &person[pos].address[pos].number);
	printf("Area:");
	scanf("%s", &person[pos].address[pos].area);
	printf("TK:");
	scanf("%d", &person[pos].address[pos].TK);
	system("cls");
}


void calculate_cost()																							/*sinartish calculate cost*/
{
	int i,d,month,sec,date,day,f=0,j,pos2,kap,kpe,sk;
	char temp[4],destination[4],des1[7],des2[7];
	for(i=0;i<=2;i++)
	{
		do																										/*elegxos egirothtas*/
		{
			f=0;
			printf("FLIGHT NO.%d\n",i+1);
			printf("Type your detsintation:");																	/*zhteitai o proorismos*/
			scanf("%s",destination);
			system("cls");
			j=0;
			while((j<10)&&(f==0))																				/*anazhthsh gia ton proorismo poy thelei o xrhsths*/
			{
				if (strcmp(destination,p[j].AP)==0)
				{
					f=1;
					pos2=j;
				}
				else
				{
					j++;
				}
			}
			if (f==0)
			{
				printf("Destination not found\n");
			}
		}
		while(f==0);
	  	time_t t = time(NULL);
  		struct tm tm = *localtime(&t);																						
		date=tm.tm_mon+1+ tm.tm_mday*100;
		itoa(date,temp,10);																				
		
		strcpy(person[pos].flight[i][0],"ATH");
		strcat(person[pos].flight[i][0],destination);
		strcat(person[pos].flight[i][0],temp);
		
		strcpy(person[pos].flight[i][1],destination);
		strcat(person[pos].flight[i][1],"ATH");
		strcat(person[pos].flight[i][1],temp);
		
		kap= p[pos2].FA+p[pos2].KP;																					/*ypologismos kostous*/
		kpe= p[0].FA+p[0].KP;	
		sk=kap+kpe;
		itoa(sk,temp,10);
		strcpy(person[pos].flight[i][2],temp);
											
		strcpy(person[pos].flight[i][3],destination);
		
	}																									
}


void payment()																										/*klhsh sinarthshs payment*/
{
	int i,k,cost,option,num;	
	for (i=0;i<=2;i++)																								/*emfanisi pthsewn*/
	{
		printf("Flight No.%d: %s\t %s\n",i+1, person[pos].flight[i][3], person[pos].flight[i][2]);
	}
	do
	{	
		k=-1;
		printf("Place the flight destination you want to pay(1-3):");
		scanf("%d", &option);
		i=0;
		while((i<3)&&(k==(-1)))																					/*anazhthsh an yparxei afto pou pliktrologhse o xrhsths*/
		{
			if (option==(i+1))
			{
				k=i;
				num=atoi(person[pos].flight[k][2]);
			}
			i++;
		}
		if (k==(-1))
		{
			printf("INVALID ANSWER\n");
		}
	}
	while(k==(-1));
	do																												/*elegxos egirothtas an to kostos pou plhktrologei o xrhsths alhthevei*/
	{
		printf("Type the cost:");
		scanf("%d", &cost);
	}
	while(cost!=num);
	payed[pos][k]=1;																								/*exei plhrwthei afth h plthsh*/
	system("cls");
}

void most_expensive()																								/*synartish most_expensive*/
 {
 	system("cls");
 	int i,max=0,pmax=0,num;
 	for(i=0;i<=2;i++)																								/*anazhthsh*/
 	{
 		if (payed[pos][i]==1)																						/*an exei plhrvsei*/
 		{
 			num=atoi(person[pos].flight[pos][i]);
 			if (num>max)
 			{
 				pmax=i;
 				max=num;
			}
		}
	}
	printf("%s\t%s\t%s\t%s\n",person[pos].flight[pmax][0],person[pos].flight[pmax][1],person[pos].flight[pmax][2],person[pos].flight[pmax][3]);		/*ektipwsh akrivoterh plhrwmenh pthsh*/

}

