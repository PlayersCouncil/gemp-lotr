package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_008_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes of
	// turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dori", "30_8");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability only
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] Dwarf companion, 6/3, damage +1; a skirmish ability used only on purpose
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion who is not Bilbo, vitality 4; +2
					// only while he bears a weapon. The Hobbit sets print no [shire] companion but Bilbo.
					put("merry", "1_303");

					// The Witch-King, Revived: [wraith] Nazgûl, strength 14, fierce; a skirmish ability used only on purpose
					put("witchking", "32_67");
					// Orkish Aggressor: [gundabad] Orc, strength 6; an assignment ability used only on purpose
					put("aggressor", "30_39");
					// Wicked Spider: [spider] Spider, strength 7, fierce; a skirmish ability that wants an Orc to discard
					put("spider", "31_55");
					// Lavastone Giant: [moria] Giant, strength 14; a Shadow ability from the discard pile and an
					// archery ability used only on purpose
					put("giant", "33_44");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DoriStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Dori, Really a decent fellow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Follower
		 * Subtype: Dwarf
		 * Strength: +1
		 * Aid: Exert Bilbo.
		 * Game Text: <b>Aid</b> – Exert Bilbo. <helper>(At the start of the maneuver phase, you may exert Bilbo to transfer this to a companion.)</helper>
		 * 	Bearer may not be overwhelmed unless his strength is tripled.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dori");

		assertEquals("Dori", card.getBlueprint().getTitle());
		assertEquals("Really a decent fellow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void AidExertsBilboToTransferDoriToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var dori = scn.GetFreepsCard("dori");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(dori);

		scn.StartGame();
		scn.MoveMinionsToTable("spider");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, dori);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dori, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		// Bilbo is the one who can pay: he exerts unasked, and Balin, who could, does not.
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));

		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, balin));
		scn.FreepsChooseCard(balin);
		assertAttachedTo(dori, balin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AidIsNotOfferedWhileBilboIsExhaustedThoughAnotherShireHobbitCompanionCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var dori = scn.GetFreepsCard("dori");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(dori);
		// Vitality 4 with 3 wounds: Bilbo cannot exert.
		scn.AddWoundsToChar(bilbo, 3);

		scn.StartGame();
		scn.MoveMinionsToTable("spider");

		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dori, "Aid"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, dori);
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(merry));
	}

	@Test
	public void BearerShortOfTripledIsWoundedAndNotOverwhelmedWhileAnotherCompanionAtDoubleIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dori = scn.GetFreepsCard("dori");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var witchking = scn.GetShadowCard("witchking");
		var aggressor = scn.GetShadowCard("aggressor");
		var giant = scn.GetShadowCard("giant");
		scn.MoveCompanionsToTable(balin, gloin);
		scn.AttachCardsTo(gloin, dori);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, aggressor, giant);

		assertAttachedTo(dori, gloin);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gloin, witchking}, new PhysicalCardImpl[]{balin, giant});
		scn.ShadowAssignToMinions(gloin, aggressor);

		// Balin 7, who does not bear Dori, against the Giant's 14: double, and he is overwhelmed.
		scn.FreepsResolveSkirmish(balin);
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(14, scn.GetStrength(giant));
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, balin);

		// Glóin 6 + 1 for Dori is 7, against 14 + 6 = 20: almost triple, and he only takes a wound.
		scn.FreepsResolveSkirmish(gloin);
		assertEquals(7, scn.GetStrength(gloin));
		assertEquals(14, scn.GetStrength(witchking));
		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(0, scn.GetWoundsOn(gloin));
		scn.PassSkirmishActions();

		assertInZone(Zone.FREE_CHARACTERS, gloin);
		assertEquals(1, scn.GetWoundsOn(gloin));
		assertAttachedTo(dori, gloin);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void BearerAtTripledStrengthIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dori = scn.GetFreepsCard("dori");
		var gloin = scn.GetFreepsCard("gloin");
		var witchking = scn.GetShadowCard("witchking");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(gloin);
		scn.AttachCardsTo(gloin, dori);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, spider);

		assertAttachedTo(dori, gloin);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gloin, witchking);
		scn.ShadowAssignToMinions(gloin, spider);

		// Glóin 6 + 1 for Dori is 7, against 14 + 7 = 21: triple.
		scn.FreepsResolveSkirmish(gloin);
		assertEquals(7, scn.GetStrength(gloin));
		assertEquals(14, scn.GetStrength(witchking));
		assertEquals(7, scn.GetStrength(spider));
		assertEquals(0, scn.GetWoundsOn(gloin));
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, gloin);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
