package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mercs", "10_42");
					put("mercs2", "10_42");
					put("mercs3", "10_42");
					// Easterling Attacker: a [raider] Easterling Man, strength 6; "Skirmish: Exert this minion to make
					// him damage +1", not used
					put("attacker", "7_141");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul, no Man; his text needs 3 burdens
					put("khamul", "103_61");
					// Dunlending Brigand: a [dunland] Man, a Man who is not [raider]; no text
					put("brigand", "4_10");

					// Six companions besides Frodo
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound; a maneuver ability not used
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound; a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his text is his own strength
					put("knight", "5_35");      // Gondorian Knight: Knight is his whole text
					// Elrond, Lord of Rivendell: an ally whose home is site 3, where he may be assigned; his
					// start-of-turn heal and fellowship draw are not used
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarHaradMercenariesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Far Harad Mercenaries
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br>At the start of each skirmish involving this minion, you may spot 6 companions and another [raider] Man to discard an unbound companion this minion is skirmishing.
		*/

		var scn = GetScenario();

		var mercs = scn.GetFreepsCard("mercs");

		assertEquals("Far Harad Mercenaries", mercs.getBlueprint().getTitle());
		assertNull(mercs.getBlueprint().getSubtitle());
		assertFalse(mercs.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, mercs.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, mercs.getBlueprint().getCulture());
		assertEquals(CardType.MINION, mercs.getBlueprint().getCardType());
		assertEquals(Race.MAN, mercs.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(mercs, Keyword.SOUTHRON));
		assertEquals(5, mercs.getBlueprint().getTwilightCost());
		assertEquals(11, mercs.getBlueprint().getStrength());
		assertEquals(3, mercs.getBlueprint().getVitality());
		assertEquals(4, mercs.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfItsSkirmishItMayDiscardTheUnboundCompanionItIsSkirmishingAndNotAtAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mercs = scn.GetShadowCard("mercs");
		var attacker = scn.GetShadowCard("attacker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), aragorn, gimli, scn.GetFreepsCard("boromir"),
				scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("knight"));

		scn.StartGame();
		scn.MoveMinionsToTable(mercs, attacker);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, mercs}, new PhysicalCardImpl[]{gimli, attacker});

		// Seven companions, and the Easterling is another [raider] Man.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mercs));
		assertInZone(Zone.FREE_CHARACTERS, aragorn, gimli);
		scn.ShadowAcceptOptionalTrigger();

		// Aragorn is the one unbound companion it skirmishes, discarded with nothing asked; Gimli, waiting for his
		// own skirmish against the Easterling, is not touched.
		assertInDiscard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.IsCharAssignedAgainst(gimli, attacker));

		// Gimli's skirmish does not involve the Mercenaries: no offer, with six companions still in play.
		scn.FreepsResolveSkirmish(gimli);
		assertInZone(Zone.SHADOW_CHARACTERS, mercs);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mercs));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithFiveCompanionsAndAnAllyItMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mercs = scn.GetShadowCard("mercs");
		var attacker = scn.GetShadowCard("attacker");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), aragorn, scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("boromir"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("elrond"));

		scn.StartGame();
		scn.MoveMinionsToTable(mercs, attacker);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mercs);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mercs));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithARaiderNazgulAndADunlandManButNoOtherRaiderManItMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mercs = scn.GetShadowCard("mercs");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), aragorn, scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("knight"));

		scn.StartGame();
		scn.MoveMinionsToTable(mercs, khamul, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mercs);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mercs));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CopiesSkirmishingAnAllyOrSamMakeNoOfferAndOneSkirmishingAnUnboundCompanionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mercs = scn.GetShadowCard("mercs");
		var mercs2 = scn.GetShadowCard("mercs2");
		var mercs3 = scn.GetShadowCard("mercs3");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(sam, aragorn, scn.GetFreepsCard("gimli"), scn.GetFreepsCard("boromir"),
				scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("knight"));
		scn.MoveAlliesToTable(elrond);

		// Elrond may be assigned at his home, site 3.
		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(mercs, mercs2, mercs3);

		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elrond, mercs}, new PhysicalCardImpl[]{aragorn, mercs2},
				new PhysicalCardImpl[]{sam, mercs3});

		// Each copy is another [raider] Man (a Southron) for the others, and seven companions are in play.
		// Against an ally, who is no companion: no offer.
		scn.FreepsResolveSkirmish(elrond);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mercs));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// 11 against 8: Elrond takes a wound and lives.
		scn.PassSkirmishActions();
		assertInZone(Zone.SUPPORT, elrond);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mercs2));
		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(aragorn);

		// Against Sam, Ring-bound, with six companions still in play: no offer.
		scn.FreepsResolveSkirmish(sam);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mercs3));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DeclinedNothingIsDiscardedAndItOffersAgainInItsSkirmishAfterTheNextMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mercs = scn.GetShadowCard("mercs");
		var attacker = scn.GetShadowCard("attacker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		// Frodo and four companions: exactly the six that are spotted.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), aragorn, gimli, scn.GetFreepsCard("boromir"),
				scn.GetFreepsCard("gandalf"));

		scn.StartGame();
		scn.MoveMinionsToTable(mercs, attacker);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mercs);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mercs));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 11 against 8: Aragorn takes a wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The regroup move, and the Mercenaries' skirmish at site 3, now against Gimli, a Dwarf of another culture.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(gimli, mercs);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mercs));
		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// His was the only skirmish: it is over at once.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
