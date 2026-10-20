package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galdor", "3_18");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, not an archer; her response is only for the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion; a maneuver ability used only on purpose
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally; her text needs Freeps' site 3 in play
					put("barliman", "1_70");    // Barliman Butterbur: Man ally; a fellowship ability used only on purpose
					put("shoulder", "1_59");    // Shoulder to Shoulder: [elven] support-area condition, a card of the culture that is no Elf; a maneuver ability only

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("armory", "1_173");     // Goblin Armory: [moria] support-area condition, a Shadow card that is no minion; answers only [moria] weapons played and [moria] Orcs' wounds
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GaldorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Galdor, Councilor From the West
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: To play, spot an Elf.<br><b>Regroup:</b> Exert Galdor and spot a minion to return that minion to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("galdor");

		assertEquals("Galdor", card.getBlueprint().getTitle());
		assertEquals("Councilor From the West", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedThenPlaysToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galdor = scn.GetFreepsCard("galdor");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var shoulder = scn.GetFreepsCard("shoulder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToHand(arwen, galdor);

		scn.StartGame();

		// A Man companion, a Man ally and an [elven] condition in play, and no Elf.
		assertInZone(Zone.HAND, galdor);
		assertFalse(scn.FreepsPlayAvailable(galdor));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(galdor));

		scn.FreepsPlayCard(galdor);
		assertInZone(Zone.SUPPORT, galdor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyIsEnoughToPlayGaldor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galdor = scn.GetFreepsCard("galdor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(galdor);

		scn.StartGame();

		assertInZone(Zone.HAND, galdor);
		assertTrue(scn.FreepsPlayAvailable(galdor));
		scn.FreepsPlayCard(galdor);
		assertInZone(Zone.SUPPORT, galdor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionExertsGaldorToReturnOneMinionToItsOwnersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galdor = scn.GetFreepsCard("galdor");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(galdor, silinde);
		scn.MoveCompanionsToTable(aragorn);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(armory);
		assertInZone(Zone.SUPPORT, armory);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.REGROUP);

		// At site 2, away from his home at site 3: an ally's special ability is used wherever the fellowship is.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(galdor));
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage);
		assertInZone(Zone.SUPPORT, armory);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(galdor));

		scn.FreepsUseCardAction(galdor);
		assertEquals(1, scn.GetWoundsOn(galdor));
		// A minion of either culture and race; no companion, no ally and no Shadow card that is no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn, galdor, silinde, armory));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// The one chosen goes to the hand of its owner, the Shadow player; the other stays.
		assertInZone(Zone.HAND, savage);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.SUPPORT, armory);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOnlyAnUrukHaiInPlayTheRegroupActionIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galdor = scn.GetFreepsCard("galdor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(galdor);
		scn.MoveCompanionsToTable(aragorn);

		var minion = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(minion);
		scn.SkipToPhase(Phase.REGROUP);

		// The only minion to spot is an [isengard] Uruk-hai, no Orc.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(galdor));
		assertInZone(Zone.SHADOW_CHARACTERS, minion);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(galdor));

		scn.FreepsUseCardAction(galdor);
		// The one minion is chosen without a question.
		assertEquals(1, scn.GetWoundsOn(galdor));
		assertInZone(Zone.HAND, minion);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOnlyAnOrcInPlayTheRegroupActionIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galdor = scn.GetFreepsCard("galdor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(galdor);
		scn.MoveCompanionsToTable(aragorn);

		var minion = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(minion);
		scn.SkipToPhase(Phase.REGROUP);

		// The only minion to spot is a [moria] Orc, no Uruk-hai.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(galdor));
		assertInZone(Zone.SHADOW_CHARACTERS, minion);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(galdor));

		scn.FreepsUseCardAction(galdor);
		// The one minion is chosen without a question.
		assertEquals(1, scn.GetWoundsOn(galdor));
		assertInZone(Zone.HAND, minion);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionToSpotTheRegroupActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galdor = scn.GetFreepsCard("galdor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(galdor);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// Galdor can exert, and no minion was put into play: the Troop is still in the Shadow player's draw deck.
		assertEquals(0, scn.GetWoundsOn(galdor));
		assertInZone(Zone.DECK, scn.GetShadowCard("troop"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(galdor));
	}

	@Test
	public void TheActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galdor = scn.GetFreepsCard("galdor");
		scn.MoveAlliesToTable(galdor);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(galdor));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(galdor));
	}

	@Test
	public void ExhaustedGaldorCannotUseIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galdor = scn.GetFreepsCard("galdor");
		scn.MoveAlliesToTable(galdor);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(galdor, 1);
		assertEquals(1, scn.GetWoundsOn(galdor));
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		// Vitality 2 with 1 wound: he cannot exert, and a minion is there to spot.
		assertEquals(1, scn.GetWoundsOn(galdor));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(galdor));
	}
}
