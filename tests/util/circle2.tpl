$(!Test +- construct in a list)
$(list:endList)
... $(+circle:endCircle)circle was defined to be "$(circle)"$(endCircle)
... $(-circle:endCircle)This line should not print unless "$(circle)" is empty$(endCircle)$(endList)
