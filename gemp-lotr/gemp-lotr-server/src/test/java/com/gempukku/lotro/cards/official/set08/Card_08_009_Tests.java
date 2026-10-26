package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ship", "8_9");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetArcheryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ship", "8_9");
					put("legolas", "1_50");     // Legolas, Greenleaf: the Elf companion; an archer with an archery special ability
					put("garrison", "8_36");    // Garrison of Gondor: a [gondor] Man and an archer; his other text needs a fortification
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound [dwarven] Dwarf; a skirmish ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an Elf ALLY; Archery: exert him to wound an Uruk-hai (home site 6, so he does not fire at site 2)
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion who is no archer; stronger only against a Nazgul
					put("axe", "2_10");         // Hand Axe: a Dwarf's hand weapon; Archery: discard it to make the fellowship archery total +1
					put("marksmanship", "11_17"); // Elven Marksmanship: [elven] support-area condition; Archery: discard it to make the fellowship archery total +2
					put("lightfoot", "1_52");   // Lightfootedness: an archery EVENT, cost 0; spot an Elf companion to make the minion archery total -1
					put("wreathed", "1_238");   // Wreathed in Shadow: [wraith] support-area condition; Archery: remove (1) to make the fellowship archery total -1
					put("savage", "1_151");     // Uruk Savage: an Uruk-hai for Orophin to aim at; vitality 3, damage +1
					put("savage2", "1_151");
					put("bracers", "2_3");      // Dwarven Bracers: a Dwarf's possession; Response: discard it to prevent a wound to its bearer
					put("marksman", "1_176");   // Goblin Marksman: an archer minion, vitality 1, no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AGreyShipStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: A Grey Ship
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot an Elf companion and make the fellowship archery total -X (to a minimum of 0) to heal X unbound companions. You cannot use archery special abilities.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ship");

		assertEquals("A Grey Ship", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheXChosenComesOffTheFellowshipArcheryTotalAndThatManyUnboundCompanionsAreHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var frodo = scn.GetRingBearer();
		var ship = scn.GetFreepsCard("ship");
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(legolas, garrison, sam);
		scn.MoveCardsToHand(ship);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Two unbound companions and two Ring-bound ones are wounded; the Garrison twice, so that one heal shows.
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(garrison, 2);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(garrison));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Two archers: the fellowship archery total is 2.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(legolas));
		assertTrue(scn.FreepsPlayAvailable(ship));
		scn.FreepsPlayCard(ship);

		// X is anything from 0 to the archery total.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		// One unbound companion is healed, of the player's choosing; the Ring-bound ones are not on offer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, garrison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(garrison);

		// 2 - 1 off the total; one of the Garrison's two wounds healed and nobody else's.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetWoundsOn(garrison));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, ship);

		// "You cannot use archery special abilities": Legolas's is no longer offered when the player is next asked.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
	}

	@Test
	public void WithXOf2TheTotalLosesBothForTheArcheryPhaseAndTwoWoundedUnboundCompanionsAreHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var frodo = scn.GetRingBearer();
		var ship = scn.GetFreepsCard("ship");
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(legolas, garrison, aragorn, gimli);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(ship);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Three wounded unbound companions, one of them twice; a wounded Ring-bearer, ally and minion.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(garrison, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(orophin, 1);
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(garrison));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.ARCHERY);

		// Legolas and the Garrison: 2.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		scn.FreepsPlayCard(ship);
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		// Wounded unbound companions only: not the Ring-bearer, an ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli, garrison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, orophin, troop));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(aragorn, gimli);

		assertEquals(0, scn.GetFreepsArcheryTotal());   // 2 - 2
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(garrison));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInZone(Zone.DISCARD, ship);

		// Still in force as archery fire is resolved: the Troop takes no wound from the fellowship. Gone with the
		// phase.
		scn.PassArcheryActions();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void CannotBePlayedWithAnElfAllyAndNoElfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var ship = scn.GetFreepsCard("ship");
		var garrison = scn.GetFreepsCard("garrison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(garrison, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(ship);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Something to heal and an archery total to pay with.
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertInHand(ship);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(ship));
	}

	@Test
	public void ItsPlayerCannotUseArcherySpecialAbilitiesForTheRestOfThatArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var ship = scn.GetFreepsCard("ship");
		var arwen = scn.GetFreepsCard("arwen");
		var garrison = scn.GetFreepsCard("garrison");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var orophin = scn.GetFreepsCard("orophin");
		var marksmanship = scn.GetFreepsCard("marksmanship");
		var lightfoot = scn.GetFreepsCard("lightfoot");
		scn.MoveCompanionsToTable(arwen, garrison, gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(marksmanship);
		scn.MoveCardsToHand(ship, lightfoot);

		var wreathed = scn.GetShadowCard("wreathed");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCardsToSupportArea(wreathed);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		// Arwen is the one Elf companion. Archery special abilities of a companion's possession, an ally and a
		// condition are all on offer before the event, and so is an archery event.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(orophin));
		assertTrue(scn.FreepsActionAvailable(axe));
		assertTrue(scn.FreepsActionAvailable(marksmanship));
		assertTrue(scn.FreepsPlayAvailable(lightfoot));
		assertTrue(scn.FreepsPlayAvailable(ship));
		scn.FreepsPlayCard(ship);
		scn.FreepsChoose("0");
		assertInZone(Zone.DISCARD, ship);

		// The Shadow player's archery special ability is not his to bar.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(wreathed));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(orophin));
		assertFalse(scn.FreepsActionAvailable(axe));
		assertFalse(scn.FreepsActionAvailable(marksmanship));
		// An event is no special ability.
		assertTrue(scn.FreepsPlayAvailable(lightfoot));

		// A second archery phase in the same turn, after a second move: the abilities are back.
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(savage2);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(orophin));
		assertTrue(scn.FreepsActionAvailable(axe));
		assertTrue(scn.FreepsActionAvailable(marksmanship));
	}

	@Test
	public void AResponseAbilityInTheArcheryPhaseIsNoArcherySpecialAbilityAndIsStillUsable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var ship = scn.GetFreepsCard("ship");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var bracers = scn.GetFreepsCard("bracers");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.AttachCardsTo(gimli, bracers);
		scn.MoveCardsToHand(ship);

		var marksman = scn.GetShadowCard("marksman");

		assertAttachedTo(bracers, gimli);
		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);

		// Legolas's archery ability is on offer before the event and barred after it; X of 0 leaves the total at 1.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsPlayCard(ship);
		scn.FreepsChoose("0");
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
		scn.PassArcheryActions();


		// A Response is no archery special ability: after the event the Marksman's one archery wound is placed on
		// Gimli (the fellowship's own wound kills the Marksman, vitality 1, and leaves no assignment phase), and
		// his Bracers can still answer it.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bracers));
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, bracers);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
