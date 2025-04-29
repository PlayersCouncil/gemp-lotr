package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_019_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("aragorn", "101_19");
					put("arwen", "1_30");
					put("elrond", "1_40");
					put("galadriel", "1_45");
					put("celeborn", "1_34");
					put("orophin", "1_56");
					put("defiance", "1_37");

					put("runner", "1_178");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Aragorn, Estel
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: When you play Aragorn (except in your starting fellowship), you may play an Elf with a twilight cost of 2 or less from your draw deck.
		 * Skirmish: Discard an [elven] card from hand to make Aragorn strength +2 or <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Estel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void EstelDoesNotPlayElfIfPlayedInStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		//scn.StartGame();
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsChooseCard(aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}

	@Test
	public void EstelOnPlayTutorsAnElfOfCost2OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.FreepsMoveCardToHand(aragorn);

		scn.StartGame();

		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		//Orophin, Celeborn, or Arwen, but not Elrond (4) or Galadriel (3)
		assertTrue(scn.FreepsHasCardChoicesAvailable(orophin, celeborn, arwen));
		assertFalse(scn.FreepsHasCardChoicesAvailable(elrond, galadriel));
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(Zone.DECK, orophin.getZone());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetFreepsDeckCount());

		scn.FreepsChooseCardBPFromSelection(orophin);
		assertEquals(Zone.SUPPORT, orophin.getZone());
	}

	@Test
	public void SkirmishAbilityCanDiscardsElvenCardToPumpAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defiance = scn.GetFreepsCard("defiance");
		var arwen = scn.GetFreepsCard("arwen");

		scn.FreepsMoveCharToTable(aragorn);
		scn.FreepsMoveCardToHand(defiance, arwen);

		var runner = scn.GetShadowCard("runner");

		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(Zone.HAND, defiance.getZone());
		assertEquals(Zone.HAND, arwen.getZone());
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsUseCardAction(aragorn);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(arwen, defiance));
		scn.FreepsChooseCard(defiance);

		assertEquals(Zone.DISCARD, defiance.getZone());
		assertEquals(Zone.HAND, arwen.getZone());

		scn.FreepsChoose("strength");
		assertEquals(10, scn.GetStrength(aragorn));

	}

	@Test
	public void SkirmishAbilityCanDiscardsElvenCardToAddDamage() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defiance = scn.GetFreepsCard("defiance");
		var arwen = scn.GetFreepsCard("arwen");

		scn.FreepsMoveCharToTable(aragorn);
		scn.FreepsMoveCardToHand(defiance, arwen);

		var runner = scn.GetShadowCard("runner");

		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(Zone.HAND, defiance.getZone());
		assertEquals(Zone.HAND, arwen.getZone());
		assertFalse(scn.hasKeyword(aragorn, Keyword.DAMAGE));

		scn.FreepsUseCardAction(aragorn);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(arwen, defiance));
		scn.FreepsChooseCard(defiance);

		assertEquals(Zone.DISCARD, defiance.getZone());
		assertEquals(Zone.HAND, arwen.getZone());

		scn.FreepsChoose("damage");
		assertTrue(scn.hasKeyword(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

	}
}
