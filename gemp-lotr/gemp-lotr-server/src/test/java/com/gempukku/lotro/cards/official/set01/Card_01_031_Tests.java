
package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class Card_01_031_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("asfaloth", "1_31");
                    put("arwen", "3_7");
                    put("legolas", "1_50");
                    put("erestor", "3_14");
                    put("orophin", "1_56");
                }},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void AsfalothStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Asfaloth
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 2
		 * Game Text: Bearer must be an Elf. When played on Arwen, Asfaloth's twilight cost is -2.<br>While at a plains site, bearer is strength +2. Discard Asfaloth when at an underground site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("asfaloth");

		assertEquals("Asfaloth", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.FreepsMoveCardToHand(asfaloth, arwen, erestor, orophin);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(asfaloth));
		scn.FreepsPlayCard(orophin);
		assertTrue(scn.FreepsPlayAvailable(asfaloth));
		scn.FreepsPlayCard(arwen);
		scn.FreepsPlayCard(asfaloth);
		assertEquals(2, scn.FreepsGetCardChoiceCount()); // can go on either arwen or orophin
	}

	@Test
	public void TwilightCost2OnNonArwen() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.FreepsMoveCharToTable(arwen, erestor, orophin);
		scn.FreepsMoveCardToHand(asfaloth);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(erestor);
		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void TwilightCost0OnArwen() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.FreepsMoveCharToTable(arwen, erestor, orophin);
		scn.FreepsMoveCardToHand(asfaloth);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(arwen);
		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void BearerIsStrengthPlus2AtPlains() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.FreepsMoveCharToTable(arwen, erestor, orophin);
		scn.FreepsMoveCardToHand(asfaloth);

		//cheating to ensure site 2 qualifies
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.PLAINS));

		scn.StartGame();

		assertEquals(6, scn.GetStrength(arwen));
		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(arwen);
		assertEquals(8, scn.GetStrength(arwen));
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(10, scn.GetStrength(arwen));
	}

	@Test
	public void SelfDiscardsAtUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.FreepsMoveCharToTable(arwen, erestor, orophin);
		scn.FreepsMoveCardToHand(asfaloth);

		//cheating to ensure site 2 qualifies
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.UNDERGROUND));

		scn.StartGame();

		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(arwen);
		assertEquals(Zone.ATTACHED, asfaloth.getZone());
		scn.FreepsPassCurrentPhaseAction();

		scn.FreepsChooseAny(); // timing tie between Asfaloth and Site
		assertEquals(Zone.DISCARD, asfaloth.getZone());
	}

	@Test
	public void TransferBetweenAllyAndCompanionPaysTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		// Bug reported: https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=FallsLover$ilpahhdbsdmp8i2x
		// Site 6 permitted multiple transfers between Rumil and Legolas on site 6 with 0 twilight added to the pool.

		//Pre-game setup
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.FreepsMoveCharToTable(arwen, legolas, erestor, orophin);
		scn.FreepsMoveCardToHand(asfaloth);

		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(erestor);
		assertEquals(2, scn.GetTwilight());

		scn.SkipToSite(3);

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsTransferAvailable(asfaloth));
		scn.FreepsTransferCard(asfaloth);
		scn.FreepsChooseCard(legolas);
		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void Legacy_playDiscountAsfalothOnArwen() throws DecisionResultInvalidException, CardNotFoundException {
		var legacy = new LegacyAtTest();
		legacy.playDiscountAsfalothOnArwen();
	}

	@Test
	public void Legacy_playDiscountAsfalothOnOtherElf() throws DecisionResultInvalidException, CardNotFoundException {
		var legacy = new LegacyAtTest();
		legacy.playDiscountAsfalothOnOtherElf();
	}

	//These particular tests are pretty redundant with the new ones that check for twilight results above
	// (not to mention somewhat less rigorous about it), but I am hesitant to delete these entirely only
	// because they check for certain things around the guts that I do not--for instance, the specific
	// existence of arguments instead of just card id counts, or the existence of the specific kind of
	// card action choice.
	//As I do not understand enough about the system to know what can and cannot be safely discarded, these
	// will simply have to stay.
	public static class LegacyAtTest extends AbstractAtTest {

		public void playDiscountAsfalothOnArwen() throws DecisionResultInvalidException {
			Map<String, Collection<String>> extraCards = new HashMap<>();
			extraCards.put(P1, Arrays.asList("1_30", "1_31"));
			initializeSimplestGame(extraCards);

			// Play first character
			AwaitingDecision firstCharacterDecision = _userFeedback.getAwaitingDecision(P1);
			assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, firstCharacterDecision.getDecisionType());
			validateContents(new String[]{"1_30"}, ((String[]) firstCharacterDecision.getDecisionParameters().get("blueprintId")));

			playerDecided(P1, getArbitraryCardId(firstCharacterDecision, "1_30"));

			skipMulligans();

			PhysicalCard asfaloth = _game.getGameState().getHand(P1).get(0);

			final AwaitingDecision awaitingDecision = _userFeedback.getAwaitingDecision(P1);
			assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, awaitingDecision.getDecisionType());
			validateContents(new String[]{"" + asfaloth.getCardId()}, (String[]) awaitingDecision.getDecisionParameters().get("cardId"));

			assertEquals(0, _game.getGameState().getTwilightPool());

			playerDecided(P1, "0");

			assertEquals(0, _game.getGameState().getTwilightPool());
			assertEquals(Zone.ATTACHED, asfaloth.getZone());
		}

		public void playDiscountAsfalothOnOtherElf() throws DecisionResultInvalidException {
			Map<String, Collection<String>> extraCards = new HashMap<>();
			extraCards.put(P1, Arrays.asList("1_51", "1_31"));
			initializeSimplestGame(extraCards);

			// Play first character
			AwaitingDecision firstCharacterDecision = _userFeedback.getAwaitingDecision(P1);
			assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, firstCharacterDecision.getDecisionType());
			validateContents(new String[]{"1_51"}, ((String[]) firstCharacterDecision.getDecisionParameters().get("blueprintId")));

			playerDecided(P1, getArbitraryCardId(firstCharacterDecision, "1_51"));

			skipMulligans();

			PhysicalCard asfaloth = _game.getGameState().getHand(P1).get(0);

			final AwaitingDecision awaitingDecision = _userFeedback.getAwaitingDecision(P1);
			assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, awaitingDecision.getDecisionType());
			validateContents(new String[]{"" + asfaloth.getCardId()}, (String[]) awaitingDecision.getDecisionParameters().get("cardId"));

			assertEquals(0, _game.getGameState().getTwilightPool());

			playerDecided(P1, "0");

			assertEquals(2, _game.getGameState().getTwilightPool());
			assertEquals(Zone.ATTACHED, asfaloth.getZone());
		}
	}

}
