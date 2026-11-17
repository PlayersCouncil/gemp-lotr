package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_123_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "13_123");
					put("ridersmount", "4_287"); // Rider's Mount: [rohan] mount, cost 1, on a [rohan] Man; a start-of-skirmish exertion only
					// Horse of Rohan: [rohan] mount, cost 2, on a Man, Elf or Wizard; a start-of-skirmish exertion only
					put("horse", "4_283");
					// Sturdy Shield: [rohan] possession, a shield and no mount, on a [rohan] Man; strength +1 while no
					// opponent controls a site, and a regroup ability used only on purpose
					put("shield", "15_141");
					// Éowyn, Sister-daughter of Théoden: [rohan] Man companion, strength 6; Valiant, and a response only
					// while she is exhausted
					put("eowyn", "4_271");
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit, strength 3; his Man text, exertion
					// response and skirmish ability do not reach these tests
					put("merry", "103_85");
					// Rohirrim Herdsman: a [rohan] Man ally, strength 4; a regroup ability used only on purpose
					put("herdsman", "7_247");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					// Thrarin, Dwarven Smith: a [dwarven] ally; his maneuver ability has him participate in skirmishes
					put("thrarin", "1_27");

					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("brigand2", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Éomer, Heir to Meduseld
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While Éomer bears a mount, he is <b>damage +1</b>.<br>While no other companion is assigned to a skirmish, Éomer is strength +1 for each other [rohan] companion you can spot.
		*/

		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", eomer.getBlueprint().getTitle());
		assertEquals("Heir to Meduseld", eomer.getBlueprint().getSubtitle());
		assertTrue(eomer.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, eomer.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, eomer.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, eomer.getBlueprint().getCardType());
		assertEquals(Race.MAN, eomer.getBlueprint().getRace());
		assertEquals(3, eomer.getBlueprint().getTwilightCost());
		assertEquals(7, eomer.getBlueprint().getStrength());
		assertEquals(3, eomer.getBlueprint().getVitality());
		assertEquals(7, eomer.getBlueprint().getResistance());
	}

	@Test
	public void IsDamagePlusOneOnceAMountIsPlayedOnHimAndNotForAPossessionThatIsNoMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var eowyn = scn.GetFreepsCard("eowyn");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		var horse = scn.GetFreepsCard("horse");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(eomer, eowyn);
		// Éomer bears a possession that is no mount; Éowyn, another [rohan] Man, bears a mount.
		scn.AttachCardsTo(eomer, shield);
		scn.AttachCardsTo(eowyn, horse);
		scn.MoveCardsToHand(ridersmount);

		scn.StartGame();
		assertAttachedTo(shield, eomer);
		assertAttachedTo(horse, eowyn);
		assertInHand(ridersmount);
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(eowyn, Keyword.DAMAGE));

		// Éowyn bears a mount already, so Éomer is the only bearer for it.
		scn.FreepsPlayCard(ridersmount);
		assertAttachedTo(ridersmount, eomer);
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(eowyn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlusOneForEachOtherRohanCompanionAndNotForAnAllyOrACompanionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var eowyn = scn.GetFreepsCard("eowyn");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eomer, merry, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(eowyn);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, eomer, merry, aragorn);
		assertInZone(Zone.SUPPORT, herdsman);
		assertInHand(eowyn);
		// Nobody is assigned. Merry, a [rohan] Hobbit, counts; the [rohan] ally, the [gondor] Aragorn, Frodo and
		// Éomer himself do not: 7 + 1.
		assertEquals(8, scn.GetStrength(eomer));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(herdsman));

		scn.FreepsPlayCard(eowyn);
		assertInZone(Zone.FREE_CHARACTERS, eowyn);
		// Merry and Éowyn: 7 + 2.
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void LosesTheBonusOnceAnotherCompanionIsAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eomer, eowyn);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		// Nobody is assigned yet: 7 + 1 for Éowyn.
		assertEquals(8, scn.GetStrength(eomer));

		scn.FreepsAssignToMinions(eowyn, brigand);
		assertTrue(scn.IsCharAssigned(eowyn));
		assertFalse(scn.IsCharAssigned(eomer));
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(eowyn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void KeepsTheBonusWhileOnlyHeAndAnAllyAreAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var eowyn = scn.GetFreepsCard("eowyn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(eomer, eowyn);
		scn.MoveAlliesToTable(thrarin);

		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		// Thrarin exerts to take part in skirmishes.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eomer, brigand}, new PhysicalCardImpl[]{thrarin, brigand2});
		assertTrue(scn.IsCharAssigned(eomer));
		assertTrue(scn.IsCharAssigned(thrarin));
		assertFalse(scn.IsCharAssigned(eowyn));
		// Éomer himself and an ally are assigned, no other companion: 7 + 1 for Éowyn.
		assertEquals(8, scn.GetStrength(eomer));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
