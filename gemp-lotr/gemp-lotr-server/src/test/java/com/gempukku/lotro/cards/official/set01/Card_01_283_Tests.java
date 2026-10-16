package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_283_Tests
{

	// Site 1 is Bag End, no sanctuary; site 3 is Rivendell Valley and site 6 Galadriel's Glade, each a sanctuary.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("evil", "1_283");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, cost 2, site 6; his text answers a weapon played on him
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability used only on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai (no Orc), damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf companion, vitality 3; a skirmish ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion; an archery ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, not a companion; a fellowship ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion, vitality 4; strength +2 with a weapon
					put("appetite", "1_294");   // Hobbit Appetite: [shire] Fellowship event, "Add (X) to heal a Hobbit companion X times"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YouBringGreatEvilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: You Bring Great Evil
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, spot a [sauron] Orc. Plays on a companion (except an Elf).<br>While at a sanctuary, bearer cannot heal.
		*/

		var scn = GetScenario();

		var evil = scn.GetFreepsCard("evil");

		assertEquals("You Bring Great Evil", evil.getBlueprint().getTitle());
		assertNull(evil.getBlueprint().getSubtitle());
		assertFalse(evil.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, evil.getBlueprint().getSide());
		assertEquals(Culture.SAURON, evil.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, evil.getBlueprint().getCardType());
		assertEquals(1, evil.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASauronOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(evil, chieftain);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertInZone(Zone.HAND, evil, chieftain);
		// A [sauron] Uruk-hai and a [moria] Orc: neither is a [sauron] Orc.
		scn.MoveMinionsToTable(sentry, troop);
		assertInZone(Zone.SHADOW_CHARACTERS, sentry, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(evil));

		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.ShadowPlayAvailable(evil));

		scn.ShadowPlayCard(evil);
		scn.ShadowChooseCard(aragorn);
		assertAttachedTo(evil, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysOnAChosenCompanionWhoIsNoElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil");
		var soldier = scn.GetShadowCard("soldier");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToHand(evil);
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		assertInZone(Zone.HAND, evil);
		// Another [sauron] Orc of another title to spot.
		scn.MoveMinionsToTable(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);
		// Companions of three cultures and races, the Ring-bearer among them; not the Elf companion, the ally
		// or the minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(legolas, barliman, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertAttachedTo(evil, gimli);
		assertNotAttachedTo(evil, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtEachSanctuaryTheBearerIsNotOfferedTheSanctuaryHeals() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, evil);

		scn.StartGame();
		assertAttachedTo(evil, aragorn);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));

		// Site 3: Gimli may be healed, and not Aragorn, who bears the condition.
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsDeclineSanctuaryHealing();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Site 6, the other sanctuary: the same.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertAttachedTo(evil, aragorn);
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsDeclineSanctuaryHealing();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AwayFromASanctuaryTheBearerIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var appetite = scn.GetFreepsCard("appetite");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, evil);
		scn.MoveCardsToHand(appetite);

		scn.StartGame();
		assertAttachedTo(evil, merry);
		assertInZone(Zone.HAND, appetite);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(frodo));

		// Site 1, Bag End, is no sanctuary: Merry may be healed though he bears the condition.
		assertEquals(1, scn.GetCurrentSiteNumber());
		scn.FreepsPlayCard(appetite);
		scn.FreepsChoose("1");
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo));
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertAttachedTo(evil, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
