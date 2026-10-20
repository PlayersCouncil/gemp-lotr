package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ravage", "4_32");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, strength 6, vitality 2; stronger and fierce only at 2 burdens
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5; its text acts only as it is played
					put("weary", "4_212");      // Weary: an [isengard] condition a companion or ally bears; its trigger needs an [isengard] tracker
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] Wizard, strength 8, vitality 4; may not be assigned to a skirmish
					put("staff", "4_174");      // Saruman's Staff: on Saruman, strength +2, fierce, damage +1; its maneuver ability lets him be assigned

					// What Frodo bears: a possession, an artifact and a condition of the Free Peoples.
					put("sword", "1_299");      // Hobbit Sword: strength +2, on a Hobbit, no text
					put("coat", "2_105");       // Mithril-coat: on Frodo; takes weapon and damage bonuses from minions skirmishing him
					put("stone", "1_314");      // Stone Trolls: a [shire] condition that plays on any character, a minion too;
					put("stone2", "1_314");     // strength +3 only against a Troll or an Uruk-hai

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3
					put("armor", "1_8");        // Dwarven Armor: a possession a Dwarf bears; changes no number
					put("armor2", "1_8");
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text in play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("shield", "1_107");     // Great Shield: a possession a Man bears; the minion archery total is -1
					put("rangersword", "1_112");// Ranger's Sword: strength +2 and damage +1, on Aragorn

					// An ally whose home is site 3, and a possession, a condition and an artifact he may bear.
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: strength 8, vitality 4; heals allies of site 3 as his turn starts
					put("elvensword", "4_64");  // Elven Sword: strength +1, on an Elf; a skirmish ability used only on purpose
					put("gilgalad", "1_66");    // The Tale of Gil-galad: vitality +1, on an Elf; a skirmish ability used only on purpose
					put("vilya", "3_27");       // Vilya: vitality +1, on Elrond; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RavagetheDefeatedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ravage the Defeated
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If your [dunland] Man wins a skirmish, discard all Free Peoples cards borne by the companion or ally he was skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ravage");

		assertEquals("Ravage the Defeated", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsEveryFreePeoplesCardTheBeatenCompanionBearsAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		var coat = scn.GetFreepsCard("coat");
		var stone = scn.GetFreepsCard("stone");
		var stone2 = scn.GetFreepsCard("stone2");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(frodo, sword, coat, stone);
		scn.AttachCardsTo(gimli, armor);

		var ravage = scn.GetShadowCard("ravage");
		var brigand = scn.GetShadowCard("brigand");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCardsToHand(ravage);
		scn.AttachCardsTo(frodo, weary);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		// The winner bears a Free Peoples card himself.
		scn.AttachCardsTo(brigand, stone2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, brigand);
		scn.FreepsResolveSkirmish(frodo);

		// 2 companions + 2 for the site
		assertEquals(4, scn.GetTwilight());
		scn.FreepsPass();
		// Nobody has won anything yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(ravage));

		// 8 against 3 + 1 for the Ring + 2 for the sword: Frodo loses.  The response to the win comes before his
		// wound for losing (engine pass 54).
		scn.ShadowPass();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowPlayAvailable(ravage));
		assertAttachedTo(sword, frodo);
		assertAttachedTo(coat, frodo);
		assertAttachedTo(stone, frodo);

		scn.ShadowPlayCard(ravage);
		// Then the wound, taken without putting on the Ring.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, ravage);
		// A possession, an artifact and a condition, with nothing chosen.
		assertInZone(Zone.DISCARD, sword, coat, stone);
		// A Shadow card he bears, and the Ring, which is no Free Peoples card.
		assertAttachedTo(weary, frodo);
		assertAttachedTo(ring, frodo);
		// A companion who was in no skirmish, and the Man who won.
		assertAttachedTo(armor, gimli);
		assertAttachedTo(stone2, brigand);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACardBorneByAMinionWhoWonBesideTheDunlandManIsNotDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var shield = scn.GetFreepsCard("shield");
		var stone = scn.GetFreepsCard("stone");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, shield);

		var ravage = scn.GetShadowCard("ravage");
		var brigand = scn.GetShadowCard("brigand");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(ravage);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, runner);
		scn.AttachCardsTo(runner, stone);
		scn.SkipToAssignments();
		// The Free Peoples player assigns the Brigand; the Runner is left over, and the Shadow player sends it in too.
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowAssignToMinions(aragorn, runner);

		// 8 + 5 against 8: Aragorn loses to the two of them.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(ravage));

		scn.ShadowPlayCard(ravage);
		assertInZone(Zone.DISCARD, shield);
		// The Runner was skirmishing Aragorn too, and is neither a companion nor an ally.
		assertAttachedTo(stone, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyBeatenAtHisHomeSiteLosesEveryFreePeoplesCardHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var elvensword = scn.GetFreepsCard("elvensword");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var vilya = scn.GetFreepsCard("vilya");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, elvensword, gilgalad, vilya);

		var ravage = scn.GetShadowCard("ravage");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(ravage);
		scn.SetTwilight(3);
		// The fellowship moves to site 3, Elrond's home, where he may be assigned to a skirmish.
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, savage);
		scn.FreepsResolveSkirmish(elrond);

		assertEquals(3, scn.GetCurrentSiteNumber());
		// 3 set + 1 companion + 0 for the site
		assertEquals(4, scn.GetTwilight());

		// 11 against 8 + 1 for the sword: Elrond loses.
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowPlayAvailable(ravage));

		scn.ShadowPlayCard(ravage);
		assertInZone(Zone.DISCARD, ravage);
		assertInZone(Zone.DISCARD, elvensword, gilgalad, vilya);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenADunlandManLosesNorWhenAManOfAnotherCultureWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var shield = scn.GetFreepsCard("shield");
		var rangersword = scn.GetFreepsCard("rangersword");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.AttachCardsTo(aragorn, shield, rangersword);
		scn.AttachCardsTo(guard, armor);

		var ravage = scn.GetShadowCard("ravage");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(ravage);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, axeman}, new PhysicalCardImpl[]{aragorn, brigand});

		// 3 companions + 2 for the site: the event could be paid for.
		assertEquals(5, scn.GetTwilight());

		// 6 against 4: the Axeman, a Man who is not [dunland], beats a companion who bears a Free Peoples card.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 8 against 8 + 2 for the sword: the Brigand, a [dunland] Man, loses.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, brigand);
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		assertInZone(Zone.HAND, ravage);
		assertAttachedTo(armor, guard);
		assertAttachedTo(shield, aragorn);
		assertAttachedTo(rangersword, aragorn);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenADunlandMinionWhoIsNoManWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, shield);

		var ravage = scn.GetShadowCard("ravage");
		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCardsToHand(ravage);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.AttachCardsTo(saruman, staff);
		// The Staff's maneuver ability makes "Saruman may not be assigned to a skirmish" not apply until the regroup phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, saruman);

		// 2 companions + 2 for the site: the event could be paid for.
		assertEquals(4, scn.GetTwilight());

		// 8 + 2 for the Staff against 8: Saruman, a [dunland] minion who is no Man, beats a companion who bears a
		// Free Peoples card.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 1 for losing + 1 for the Staff's damage +1
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.  The Staff makes
		// Saruman fierce, so the game goes on to the fierce assignments.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		assertInZone(Zone.HAND, ravage);
		assertAttachedTo(shield, aragorn);
	}
}
