
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_011_Tests
{
	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("library", "101_11");
					put("gandalf", "1_364");
					put("aragorn", "1_89");
					put("galadriel", "1_45");
					put("gimli", "1_12");

					put("dwarftale", "1_24");
					put("gondortale", "3_41");
					put("elftale", "1_66");
					put("shiretale", "2_113");

					put("dwarfart", "9_6");
					put("gandalfart", "2_22");
					put("gondorart", "9_36");
					put("elfart", "9_20");
					put("shireart", "2_105");

					put("savage1", "1_151");
					put("savage2", "1_151");
					put("savage3", "1_151");
					put("savage4", "1_151");
					put("savage5", "1_151");
					put("savage6", "1_151");
					put("savage7", "1_151");
					put("savage8", "1_151");
					put("savage9", "1_151");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void LibraryofRivendellStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Library of Rivendell
		* Side: Free Peoples
		* Culture: elven
		* Twilight Cost: 3
		* Type: artifact
		* Subtype: Support Area
		* Game Text: Fellowship: Stack your tale or artifact from play or from hand here to draw a card.
		* Regroup: Exert an [ELVEN] ally and discard this artifact to draw X cards, exert X minions, and remove up to
		 * X wounds from companions, where X is the number of Free Peoples cultures on cards stacked here.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl library = scn.GetFreepsCard("library");

		assertTrue(library.getBlueprint().isUnique());
		assertTrue(scn.hasKeyword(library, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(3, library.getBlueprint().getTwilightCost());
		assertEquals(CardType.ARTIFACT, library.getBlueprint().getCardType());
		assertEquals(Culture.ELVEN, library.getBlueprint().getCulture());
		assertEquals(Side.FREE_PEOPLE, library.getBlueprint().getSide());
	}



	@Test
	public void LibraryofRivendellStacksTalesOrArtifactsFromPlayToDrawCard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl library = scn.GetFreepsCard("library");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");

		PhysicalCardImpl dwarftale = scn.GetFreepsCard("dwarftale");
		PhysicalCardImpl elftale = scn.GetFreepsCard("elftale");
		PhysicalCardImpl gondortale = scn.GetFreepsCard("gondortale");
		PhysicalCardImpl shiretale = scn.GetFreepsCard("shiretale");

		PhysicalCardImpl dwarfart = scn.GetFreepsCard("dwarfart");
		PhysicalCardImpl elfart = scn.GetFreepsCard("elfart");
		PhysicalCardImpl gandalfart = scn.GetFreepsCard("gandalfart");
		PhysicalCardImpl gondorart = scn.GetFreepsCard("gondorart");
		PhysicalCardImpl shireart = scn.GetFreepsCard("shireart");

		scn.FreepsMoveCardToSupportArea(library);
		scn.FreepsMoveCharToTable(gandalf, aragorn, gimli, galadriel);
		scn.FreepsMoveCardToHand(dwarftale, elftale, gondortale, shiretale, dwarfart, elfart, gandalfart, gondorart, shireart);

		PhysicalCardImpl savage1 = scn.GetShadowCard("savage1");
		PhysicalCardImpl savage2 = scn.GetShadowCard("savage2");
		PhysicalCardImpl savage3 = scn.GetShadowCard("savage3");
		PhysicalCardImpl savage4 = scn.GetShadowCard("savage4");
		PhysicalCardImpl savage5 = scn.GetShadowCard("savage5");
		PhysicalCardImpl savage6 = scn.GetShadowCard("savage6");
		scn.ShadowMoveCharToTable(savage1, savage2, savage3, savage4, savage5, savage6);

		scn.StartGame();

		scn.FreepsPlayCard(dwarftale);
		scn.FreepsPlayCard(elftale);
		scn.FreepsPlayCard(gondortale);
		scn.FreepsPlayCard(shiretale);

		scn.FreepsPlayCard(dwarfart);
		scn.FreepsPlayCard(elfart);
		scn.FreepsPlayCard(gandalfart);
		scn.FreepsPlayCard(gondorart);
		scn.FreepsPlayCard(shireart);

		assertTrue(scn.FreepsActionAvailable(library));

		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(dwarftale);
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(elftale);
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(gondortale);
		assertEquals(3, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(shiretale);
		assertEquals(4, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(dwarfart);
		assertEquals(4, scn.GetFreepsHandCount());//we've hit the rule of 4

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(elfart);
		assertEquals(4, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(gandalfart);
		assertEquals(4, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(gondorart);
		assertEquals(4, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(library);
		//scn.FreepsChooseCard(shireart); // last one gets auto-picked since it's the last one
		assertEquals(4, scn.GetFreepsHandCount());

		assertEquals(9, scn.GetStackedCards(library).size());
		assertFalse(scn.FreepsActionAvailable(library));
	}

	@Test
	public void LibraryofRivendellStacksTalesOrArtifactsFromHandToDrawCard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl library = scn.GetFreepsCard("library");

		PhysicalCardImpl dwarftale = scn.GetFreepsCard("dwarftale");
		PhysicalCardImpl elftale = scn.GetFreepsCard("elftale");
		PhysicalCardImpl gondortale = scn.GetFreepsCard("gondortale");
		PhysicalCardImpl shiretale = scn.GetFreepsCard("shiretale");

		PhysicalCardImpl dwarfart = scn.GetFreepsCard("dwarfart");
		PhysicalCardImpl elfart = scn.GetFreepsCard("elfart");
		PhysicalCardImpl gandalfart = scn.GetFreepsCard("gandalfart");
		PhysicalCardImpl gondorart = scn.GetFreepsCard("gondorart");
		PhysicalCardImpl shireart = scn.GetFreepsCard("shireart");

		scn.FreepsMoveCardToSupportArea(library);
		scn.FreepsMoveCardToHand(dwarftale, elftale, gondortale, shiretale, dwarfart, elfart, gandalfart, gondorart, shireart);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(library));

		assertEquals(9, scn.GetFreepsHandCount());
		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(dwarftale);
		assertEquals(9, scn.GetFreepsHandCount()); // Stacked 1, Drew 1

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(elftale);
		assertEquals(9, scn.GetFreepsHandCount()); // Stacked 1, Drew 1

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(gondortale);
		assertEquals(9, scn.GetFreepsHandCount()); // Stacked 1, Drew 1

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(shiretale);
		assertEquals(9, scn.GetFreepsHandCount()); // Stacked 1, Drew 1

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(dwarfart);
		assertEquals(8, scn.GetFreepsHandCount());//we've hit the rule of 4, so no more drawing

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(elfart);
		assertEquals(7, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(gandalfart);
		assertEquals(6, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(library);
		scn.FreepsChooseCard(gondorart);
		assertEquals(5, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(library);
		//scn.FreepsChooseCard(shireart); // last one gets auto-picked since it's the last one
		assertEquals(4, scn.GetFreepsHandCount());

		assertEquals(9, scn.GetStackedCards(library).size());
		assertFalse(scn.FreepsActionAvailable(library));
	}

	@Test
	public void RegroupAbilitySelfDiscardsToHaveSeveralEffects() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl library = scn.GetFreepsCard("library");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");

		scn.FreepsMoveCardToSupportArea(library);

		scn.FreepsMoveCharToTable(gandalf, aragorn, gimli, galadriel);

		PhysicalCardImpl savage1 = scn.GetShadowCard("savage1");
		PhysicalCardImpl savage2 = scn.GetShadowCard("savage2");
		PhysicalCardImpl savage3 = scn.GetShadowCard("savage3");
		PhysicalCardImpl savage4 = scn.GetShadowCard("savage4");
		PhysicalCardImpl savage5 = scn.GetShadowCard("savage5");
		PhysicalCardImpl savage6 = scn.GetShadowCard("savage6");
		scn.ShadowMoveCharToTable(savage1, savage2, savage3, savage4, savage5, savage6);

		scn.StartGame();

		scn.FreepsStackCardsOn(library, "dwarfart", "elfart", "gandalfart", "gondorart", "shireart");

		scn.AddWoundsToChar(gandalf, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(gimli, 2);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(5, scn.GetStackedCards(library).size());

		assertTrue(scn.FreepsActionAvailable(library));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		scn.FreepsUseCardAction(library);
		assertEquals(1, scn.GetWoundsOn(galadriel));

		//X should be 5, so draw 5 cards
		assertEquals(5, scn.GetFreepsHandCount());

		assertTrue(scn.FreepsDecisionAvailable("Choose cards to exert"));

		String[] choices = scn.FreepsGetCardChoices().toArray(new String[0]);
		assertEquals(6, choices.length);
		scn.FreepsChoose(choices[0], choices[1], choices[2], choices[3], choices[4]);

		assertEquals(1, scn.GetWoundsOn(scn.GetShadowCardByID(choices[0])));
		assertEquals(1, scn.GetWoundsOn(scn.GetShadowCardByID(choices[1])));
		assertEquals(1, scn.GetWoundsOn(scn.GetShadowCardByID(choices[2])));
		assertEquals(1, scn.GetWoundsOn(scn.GetShadowCardByID(choices[3])));
		assertEquals(1, scn.GetWoundsOn(scn.GetShadowCardByID(choices[4])));

		assertTrue(scn.FreepsDecisionAvailable("Choose cards to heal"));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(gandalf);
		scn.FreepsChoose(""); // skipping the last, since it's "up to" X

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(gandalf));

		assertEquals(Zone.DISCARD, library.getZone());
	}
}
