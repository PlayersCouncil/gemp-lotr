package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rally", "5_38");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man knight companion, no other text
					// Pippin, Trollslayer: a [gondor] Hobbit knight companion; his skirmish ability is never used
					put("pippin", "103_31");
					// Gondorian Merchant: a [gondor] Man ally of vitality 1; Armor of the White City makes him a knight,
					// and Narsil's vitality +1 lets him exert (its wound response never comes up)
					put("merchant", "7_97");
					put("whitearmor", "18_39");
					put("narsil", "9_34");
					// Ranger of Osgiliath: a [gondor] Man companion who is no knight; his regroup ability is never used
					put("ranger", "7_116");
					put("wall", "5_33");        // City Wall: a [gondor] fortification
					put("citadel", "5_32");     // Citadel of the Stars: a [gondor] fortification
					put("tower", "5_39");       // Stone Tower: a [gondor] fortification, left in the support area
					// Disarmed: a [gondor] condition a minion bears that is no fortification
					put("disarmed", "18_46");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, no text
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, damage +1, no other text
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RallyPointStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Rally Point
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Regroup:</b> Exert a knight to transfer a [gondor] fortification borne by a minion to your support area.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rally");

		assertEquals("Rally Point", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityExertsAKnightToTransferAGondorFortificationFromAMinionToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetFreepsCard("rally");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var whitearmor = scn.GetFreepsCard("whitearmor");
		var narsil = scn.GetFreepsCard("narsil");
		var ranger = scn.GetFreepsCard("ranger");
		var wall = scn.GetFreepsCard("wall");
		var citadel = scn.GetFreepsCard("citadel");
		var tower = scn.GetFreepsCard("tower");
		var disarmed = scn.GetFreepsCard("disarmed");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(knight, pippin, ranger);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, whitearmor, narsil);
		scn.MoveCardsToSupportArea(rally, tower);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Minions that lived through the skirmishes, two of two cultures and races bearing a [gondor] fortification
		// each, one bearing a [gondor] condition that is no fortification.
		scn.MoveMinionsToTable(troop, savage, brigand);
		scn.AttachCardsTo(troop, wall);
		scn.AttachCardsTo(savage, citadel);
		scn.AttachCardsTo(brigand, disarmed);
		scn.FreepsRefreshDecision();
		assertAttachedTo(wall, troop);
		assertAttachedTo(citadel, savage);
		assertAttachedTo(disarmed, brigand);
		assertAttachedTo(whitearmor, merchant);
		assertAttachedTo(narsil, merchant);
		assertInZone(Zone.SUPPORT, tower);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(ranger));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(rally));
		scn.FreepsUseCardAction(rally);
		// Any knight: a Man companion, a Hobbit companion and an ally; not a [gondor] Man who is no knight.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, pippin, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(ranger));

		// Either [gondor] fortification a minion bears; not the [gondor] condition that is no fortification, nor
		// the fortification already in the support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wall, citadel));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(disarmed, tower));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(wall);

		assertInZone(Zone.SUPPORT, wall);
		assertNotAttachedTo(wall, troop);
		assertAttachedTo(citadel, savage);
		assertAttachedTo(disarmed, brigand);
		assertInZone(Zone.SUPPORT, tower);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileTheOnlyKnightIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetFreepsCard("rally");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var wall = scn.GetFreepsCard("wall");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight, ranger);
		scn.MoveCardsToSupportArea(rally);

		scn.StartGame();
		// Vitality 3 with 2 wounds: the knight cannot exert. The Ranger, who could, is no knight.
		scn.AddWoundsToChar(knight, 2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, wall);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertAttachedTo(wall, troop);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rally));
	}

	@Test
	public void WithNoGondorFortificationBorneByAMinionTheKnightExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetFreepsCard("rally");
		var knight = scn.GetFreepsCard("knight");
		var wall = scn.GetFreepsCard("wall");
		var disarmed = scn.GetFreepsCard("disarmed");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(knight);
		// A [gondor] fortification in play, but in the support area and borne by no minion.
		scn.MoveCardsToSupportArea(rally, wall);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(brigand);
		scn.AttachCardsTo(brigand, disarmed);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(knight));
		assertInZone(Zone.SUPPORT, wall);
		assertAttachedTo(disarmed, brigand);

		assertTrue(scn.FreepsActionAvailable(rally));
		scn.FreepsUseCardAction(rally);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertInZone(Zone.SUPPORT, wall);
		assertAttachedTo(disarmed, brigand);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
