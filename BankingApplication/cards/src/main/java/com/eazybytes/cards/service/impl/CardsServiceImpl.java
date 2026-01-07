package com.eazybytes.cards.service.impl;

import com.eazybytes.cards.constants.CardsConstants;
import com.eazybytes.cards.dto.CardsDto;
import com.eazybytes.cards.entities.Cards;
import com.eazybytes.cards.exceptions.CardAlreadyExistsException;
import com.eazybytes.cards.exceptions.ResourceNotFoundException;
import com.eazybytes.cards.mapper.CardsMapper;
import com.eazybytes.cards.repository.CardsRepository;
import com.eazybytes.cards.service.ICardsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class CardsServiceImpl implements ICardsService
{
  private CardsRepository cardsRepository;
    @Override
    public void createCard(String mobileNumber) {
        Optional<Cards> isCardExisted= cardsRepository.findByMobileNumber(mobileNumber);
        if(isCardExisted.isPresent())
        {
            throw new CardAlreadyExistsException("Card already exists");
        }
        Cards newCard=createNewCard(mobileNumber);
        cardsRepository.save(newCard);
    }
    public Cards createNewCard(String mobileNumber){
      Cards newCard=new Cards();
        long randomCardNumber = 100000000000L + new Random().nextInt(900000000);
        newCard.setCardNumber(String.valueOf(randomCardNumber));
        newCard.setCardType(CardsConstants.CREDIT_CARD);
        newCard.setMobileNumber(mobileNumber);
        newCard.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
        newCard.setAmountUsed(0);
        newCard.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);
     return newCard;
    }
    public CardsDto fetchCard(String mobileNumber){
       Cards card= cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
               ()-> new ResourceNotFoundException("Card","mobileNumber",mobileNumber)
       );
       return CardsMapper.mapToCardsDto(card,new CardsDto());
    }

    @Override
    public boolean updateCard(CardsDto cardsDto) {
        Cards card=cardsRepository.findByCardNumber(cardsDto.getCardNumber()).orElseThrow(
                ()-> new ResourceNotFoundException("Card","cardNumber",cardsDto.getCardNumber())
        );
        CardsMapper.mapToCards(cardsDto,card);
        cardsRepository.save(card);
        return true;
    }
    public boolean deleteCard(String mobileNumber){
        Cards card= cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                ()-> new ResourceNotFoundException("Card","mobileNumber",mobileNumber)
        );
        cardsRepository.deleteById(card.getCardId());
        return true;
    }
}
