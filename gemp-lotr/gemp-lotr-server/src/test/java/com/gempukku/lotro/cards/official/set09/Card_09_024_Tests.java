package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("draught1", "9_24");
					put("draught2", "9_24");
					put("draught3", "9_24");
					put("draught4", "9_24");
					// Ent Avenger: [gandalf] Ent companion, strength 6, vitality 4, cost 4; strength +2 only while 3
					// Ents can be spotted (never here)
					put("avenger1", "6_27");
					put("avenger2", "6_27");
					// Treebeard, Earthborn: [gandalf] Ent ally; a response to an unbound Hobbit about to be discarded,
					// and a fellowship ability, used only on purpose
					put("treebeard", "4_103");
					// Huorn: [gandalf] Tree companion, no Ent; a response that is never set off here
					put("huorn", "9_25");
					// Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability
					// used only on purpose
					put("aragorn", "1_89");
					// Ent Moot: another [gandalf] condition, on an Ent (strength +2), limit 1 per bearer
					put("moot", "6_29");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
					// Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EntDraughtStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Ent Draught
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Vitality: 1
		 * Game Text: To play, spot an Ent. Bearer must be a companion. Limit 1 per bearer.
		 * 	<b>Skirmish:</b> Exert bearer to make him or her <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("draught1");

		assertEquals("Ent Draught", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void CannotBePlayedUntilAnEntCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draught = scn.GetFreepsCard("draught1");
		var avenger = scn.GetFreepsCard("avenger1");
		var huorn = scn.GetFreepsCard("huorn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(huorn, aragorn);
		scn.MoveCardsToHand(draught, avenger);

		scn.StartGame();

		// A Huorn is a Tree, not an Ent.
		assertInHand(draught, avenger);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(draught));

		scn.FreepsPlayCard(avenger);
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertTrue(scn.FreepsPlayAvailable(draught));

		scn.FreepsPlayCard(draught);
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(draught, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnEntAllyIsEnoughToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draught = scn.GetFreepsCard("draught1");
		var treebeard = scn.GetFreepsCard("treebeard");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(draught);

		scn.StartGame();

		assertInHand(draught);
		assertTrue(scn.FreepsPlayAvailable(draught));

		// Frodo is the only companion: it goes on him unasked.
		scn.FreepsPlayCard(draught);
		assertAttachedTo(draught, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnlyACompanionWhoBearsNoEntDraughtMayBearOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draught1 = scn.GetFreepsCard("draught1");
		var draught2 = scn.GetFreepsCard("draught2");
		var draught3 = scn.GetFreepsCard("draught3");
		var draught4 = scn.GetFreepsCard("draught4");
		var avenger1 = scn.GetFreepsCard("avenger1");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var treebeard = scn.GetFreepsCard("treebeard");
		var moot = scn.GetFreepsCard("moot");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(avenger1, avenger2);
		scn.MoveAlliesToTable(treebeard);
		scn.AttachCardsTo(avenger1, draught1);
		scn.AttachCardsTo(avenger2, moot);
		scn.MoveCardsToHand(draught2, draught3, draught4);

		scn.StartGame();

		assertAttachedTo(draught1, avenger1);
		assertAttachedTo(moot, avenger2);
		assertInHand(draught2, draught3, draught4);
		scn.FreepsPlayCard(draught2);

		// Not the Avenger who bears one already, nor the ally; the Avenger bearing another [gandalf] condition and
		// Frodo, bearing none, are offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, avenger2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger1, treebeard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(draught2, frodo);

		// The other Avenger is now the one companion who can take one.
		scn.FreepsPlayCard(draught3);
		assertAttachedTo(draught3, avenger2);
		assertAttachedTo(draught1, avenger1);

		// Every companion bears one: no copy can be played, though an Ent is spotted.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(draught4));
	}

	@Test
	public void SkirmishAbilityExertsBearerToMakeHimDamagePlusOneForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draught1 = scn.GetFreepsCard("draught1");
		var draught2 = scn.GetFreepsCard("draught2");
		var avenger = scn.GetFreepsCard("avenger1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.AttachCardsTo(avenger, draught1);
		scn.AttachCardsTo(aragorn, draught2);

		scn.StartGame();
		assertAttachedTo(draught1, avenger);
		assertAttachedTo(draught2, aragorn);
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{avenger, savage},
				new PhysicalCardImpl[]{aragorn, troop}
		);
		scn.FreepsResolveSkirmish(avenger);

		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// The text names no skirmish: the copy on Aragorn, whose skirmish is still to come, is offered, and is the one used.
		assertTrue(scn.FreepsActionAvailable(draught2));
		assertTrue(scn.FreepsActionAvailable(draught1));

		scn.FreepsUseCardAction(draught2);
		// Its own bearer exerts and gains the damage bonus, though his skirmish is still to come; the Avenger, who
		// is skirmishing, and his copy are untouched.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);

		// A skirmish ability lasts for the skirmish phase it is used in.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draught = scn.GetFreepsCard("draught1");
		var avenger = scn.GetFreepsCard("avenger1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(avenger);
		scn.AttachCardsTo(avenger, draught);

		scn.StartGame();
		// Vitality 4 + 1 from Ent Draught: 4 wounds leave him exhausted.
		scn.AddWoundsToChar(avenger, 4);
		assertEquals(4, scn.GetWoundsOn(avenger));
		assertAttachedTo(draught, avenger);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(avenger, savage);
		scn.FreepsResolveSkirmish(avenger);

		assertEquals(1, scn.GetVitality(avenger));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(draught));
	}
}
