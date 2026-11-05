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

public class Card_30_006_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes of
	// turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bofur", "30_6");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability only
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] Dwarf companion, 7/3; a maneuver ability only

					// Azog, The Defiler: [gundabad] Orc, strength 9, damage +1; a trigger on playing a mount, never reached
					put("azog", "31_23");
					// Demolition Troll: [moria] Troll, strength 10; a skirmish ability used only on purpose
					put("troll", "32_31");
					// Wicked Spider: [spider] Spider; here only a minion, so that there is a maneuver phase
					put("spider", "31_55");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BofurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Bofur, Encouraging Fellow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Follower
		 * Subtype: Dwarf
		 * Strength: +1
		 * Aid: Add a doubt.
		 * Game Text: <b>Aid</b> – Add a doubt. <helper>(At the start of the maneuver phase, you may add a doubt to transfer this to a companion.)</helper>
		 * 	<b>Response:</b> If bearer is about to take a wound, transfer Bofur to your support area to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bofur");

		assertEquals("Bofur", card.getBlueprint().getTitle());
		assertEquals("Encouraging Fellow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void AidAddsADoubtToTransferBofurToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bofur = scn.GetFreepsCard("bofur");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(bofur);

		scn.StartGame();
		scn.MoveMinionsToTable("spider");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, bofur);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bofur, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		// A doubt is the Hobbit Draft Game's burden.
		assertEquals(1, scn.GetBurdens());

		scn.FreepsChooseCard(balin);
		assertAttachedTo(bofur, balin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BearerAboutToTakeAWoundMayTransferBofurToTheSupportAreaToPreventThatWoundAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bofur = scn.GetFreepsCard("bofur");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var azog = scn.GetShadowCard("azog");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(balin, nori);
		scn.AttachCardsTo(balin, bofur);

		scn.StartGame();
		scn.MoveMinionsToTable(azog, troll);

		assertAttachedTo(bofur, balin);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, azog}, new PhysicalCardImpl[]{nori, troll});

		// Nori 7 against the Troll's 10: he loses, short of double. He does not bear Bofur: nothing is offered.
		scn.FreepsResolveSkirmish(nori);
		assertEquals(0, scn.GetWoundsOn(nori));
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bofur));
		assertEquals(1, scn.GetWoundsOn(nori));
		assertAttachedTo(bofur, balin);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Balin 7 + 1 for Bofur against Azog's 9: he loses, and takes a wound and one more for Azog's damage +1.
		scn.FreepsResolveSkirmish(balin);
		assertEquals(8, scn.GetStrength(balin));
		assertEquals(9, scn.GetStrength(azog));
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bofur));
		scn.FreepsAcceptOptionalTrigger();

		// Bofur back in his owner's support area; the first wound prevented, the second taken.
		assertInZone(Zone.SUPPORT, bofur);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bofur));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
