package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_001_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Roäc is played in
	// the fellowship phase of turn 1 at Bag End (site 1); the assignment phase is that turn's, at The Troll Hoard
	// (site 2), with the minions laid in the Shadow phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crown", "32_1");

					put("roac", "32_8");        // Roäc, Son of Carc: [dwarven] follower; his Aid and maneuver ability not used
					put("roac2", "32_8");       // a second copy, played once the first is gone
					put("dori", "30_8");        // Dori, Really a decent fellow: another [dwarven] follower; his Aid not reached
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, vitality 4; a fellowship ability not used
					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: [dwarven] ally, vitality 3; Thorin +1, a regroup trigger not reached
					put("dainco", "14_1");      // Dáin Ironfoot, King Under the Mountain: the companion of the title, vitality 3; a regroup trigger not reached
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion who is no Thorin, vitality 3; a maneuver ability not used
					put("army", "32_5");        // Iron Hills Army: [dwarven] Dwarf ally who is no Dain, vitality 3; a maneuver ability not used
					put("elfarmy", "31_8");     // Elf Army: an [elven] ally; a maneuver ability not used

					put("azog", "32_28");       // Azog, Commander of the Wolves Army: [moria] Orc; a maneuver ability at a battleground
					put("alfrid", "32_25");     // Alfrid, Lickspittle: [men] Man; his texts answer [esgaroth] allies, none in play
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, fierce; a skirmish ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void CrownofEreborStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Crown of Erebor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: Each time you play Roäc, you may heal Thorin or Dain Ironfoot. Then, you may remove a doubt.
		 * 	<b>Assignment:</b> If you cannot spot Thorin, [dwarven] allies may be assigned to skirmishes.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("crown");

		assertEquals("Crown of Erebor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayingRoacMayHealThorinOrDainAndThenRemoveADoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		var nori = scn.GetFreepsCard("nori");
		var army = scn.GetFreepsCard("army");
		scn.MoveCompanionsToTable(thorin, nori);
		scn.MoveAlliesToTable(dain, army);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.MoveCardsToHand(roac);
		scn.AddWoundsToChar(thorin, 2);
		scn.AddWoundsToChar(dain, 1);
		scn.AddWoundsToChar(nori, 1);
		scn.AddWoundsToChar(army, 1);
		scn.AddBurdens(2);
		scn.FreepsRefreshDecision();
		assertInHand(roac);
		assertInZone(Zone.SUPPORT, crown);
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(1, scn.GetWoundsOn(army));
		assertEquals(2, scn.GetBurdens());

		scn.FreepsPlayCard(roac);
		assertInZone(Zone.SUPPORT, roac);
		// Q-0414: the trigger is required, the heal a choice of 0 to 1 (Q-0314's "Then").
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(crown));

		// Thorin and Dain; not another wounded [dwarven] companion, nor another wounded Dwarf ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, dain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nori, army));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(thorin);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(1, scn.GetWoundsOn(army));

		assertTrue(scn.FreepsDecisionAvailable("remove a doubt"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DainMayBeTheOneHealedAndTheDoubtKept() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.MoveCardsToHand(roac);
		scn.AddWoundsToChar(thorin, 1);
		scn.AddWoundsToChar(dain, 2);
		scn.AddBurdens(2);
		scn.FreepsRefreshDecision();
		assertInHand(roac);
		assertInZone(Zone.SUPPORT, crown, dain);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(2, scn.GetWoundsOn(dain));
		assertEquals(2, scn.GetBurdens());

		scn.FreepsPlayCard(roac);
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, dain));
		scn.FreepsChooseCard(dain);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(dain));

		assertTrue(scn.FreepsDecisionAvailable("remove a doubt"));
		scn.FreepsChooseNo();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDainIronfootCompanionMayBeHealedWithThorinUnwounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");
		var dainco = scn.GetFreepsCard("dainco");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(thorin, dainco, nori);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.MoveCardsToHand(roac);
		scn.AddWoundsToChar(dainco, 2);
		scn.AddWoundsToChar(nori, 1);
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();
		assertInHand(roac);
		assertInZone(Zone.SUPPORT, crown);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(2, scn.GetWoundsOn(dainco));
		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsPlayCard(roac);
		// Thorin has no wound to heal, and Nori is no Dain: Dáin alone is offered, and still asked (minimum 0).
		assertTrue(scn.FreepsHasCardChoiceAvailable(dainco));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thorin, nori));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(dainco);
		assertEquals(1, scn.GetWoundsOn(dainco));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(nori));

		assertTrue(scn.FreepsDecisionAvailable("remove a doubt"));
		scn.FreepsChooseYes();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BothDeclinedHealsNothingAndRemovesNoDoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.MoveCardsToHand(roac);
		scn.AddWoundsToChar(thorin, 1);
		scn.AddWoundsToChar(dain, 1);
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();
		assertInHand(roac);
		assertInZone(Zone.SUPPORT, crown, dain);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsPlayCard(roac);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(crown));
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, dain));
		scn.FreepsDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(dain));

		assertTrue(scn.FreepsDecisionAvailable("remove a doubt"));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheHealDeclinedTheDoubtMayStillBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		// Q-0414: each "you may" is its own choice, so the doubt may be removed with Thorin and Dain left wounded.
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.MoveCardsToHand(roac);
		scn.AddWoundsToChar(thorin, 1);
		scn.AddWoundsToChar(dain, 1);
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();
		assertInHand(roac);
		assertInZone(Zone.SUPPORT, crown, dain);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsPlayCard(roac);
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, dain));
		scn.FreepsDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(dain));

		assertTrue(scn.FreepsDecisionAvailable("remove a doubt"));
		scn.FreepsChooseYes();
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingAnotherDwarvenFollowerAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var dori = scn.GetFreepsCard("dori");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.MoveCardsToHand(dori);
		scn.AddWoundsToChar(thorin, 1);
		scn.AddWoundsToChar(dain, 1);
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();
		assertInHand(dori);
		assertInZone(Zone.SUPPORT, crown, dain);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsPlayCard(dori);
		assertInZone(Zone.SUPPORT, dori);
		// Nothing is asked: no heal, no doubt.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void WithNobodyToHealTheDoubtMayStillBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.MoveCardsToHand(roac);
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();
		assertInHand(roac);
		assertInZone(Zone.SUPPORT, crown, dain);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsPlayCard(roac);
		// Nobody is wounded: no heal is asked, and the doubt's yes/no comes at once.
		assertTrue(scn.FreepsDecisionAvailable("remove a doubt"));
		scn.FreepsChooseYes();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NothingIsAskedWithNobodyToHealAndNoDoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(thorin, nori);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.MoveCardsToHand(roac);
		// Nori is wounded, but no Thorin or Dain.
		scn.AddWoundsToChar(nori, 1);
		scn.FreepsRefreshDecision();
		assertInHand(roac);
		assertInZone(Zone.SUPPORT, crown, dain);
		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(roac);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(nori));
	}

	@Test
	public void TheChoicesAreAskedAgainEachTimeRoacIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var roac = scn.GetFreepsCard("roac");
		var roac2 = scn.GetFreepsCard("roac2");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.MoveCardsToHand(roac, roac2);
		scn.AddWoundsToChar(thorin, 2);
		scn.AddBurdens(2);
		scn.FreepsRefreshDecision();
		assertInHand(roac, roac2);
		assertInZone(Zone.SUPPORT, crown, dain);
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(dain));

		scn.FreepsPlayCard(roac);
		// Dain has no wound to heal: Thorin alone is offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dain));
		scn.FreepsChooseCard(thorin);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(2, scn.GetBurdens());
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The first Roäc gone, the second is played.
		scn.MoveCardsToDiscard(roac);
		assertInZone(Zone.DISCARD, roac);
		scn.FreepsPlayCard(roac2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin));
		scn.FreepsChooseCard(thorin);
		assertEquals(0, scn.GetWoundsOn(thorin));
		scn.FreepsChooseYes();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDoubtTheRemovalIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.MoveCardsToHand(roac);
		scn.AddWoundsToChar(thorin, 1);
		scn.FreepsRefreshDecision();
		assertInHand(roac);
		assertInZone(Zone.SUPPORT, crown);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(roac);
		scn.FreepsChooseCard(thorin);
		assertEquals(0, scn.GetWoundsOn(thorin));
		// A yes would remove nothing: nothing is asked.
		assertFalse(scn.FreepsDecisionAvailable("remove a doubt"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithoutThorinTheAssignmentActionLetsEachDwarvenAllyBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var nori = scn.GetFreepsCard("nori");
		var dain = scn.GetFreepsCard("dain");
		var army = scn.GetFreepsCard("army");
		var elfarmy = scn.GetFreepsCard("elfarmy");
		var azog = scn.GetShadowCard("azog");
		var alfrid = scn.GetShadowCard("alfrid");
		// Nori, a [dwarven] companion who is no Thorin, stops nothing.
		scn.MoveCompanionsToTable(nori);
		scn.MoveAlliesToTable(dain, army, elfarmy);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, crown, dain, army, elfarmy);
		assertInZone(Zone.FREE_CHARACTERS, nori);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog, alfrid);
		assertInZone(Zone.SHADOW_CHARACTERS, azog, alfrid);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(dain));
		assertFalse(scn.FreepsCanAssign(army));
		assertTrue(scn.FreepsActionAvailable(crown));
		scn.FreepsUseCardAction(crown);

		// Both [dwarven] allies; not an ally of another culture.
		assertTrue(scn.FreepsCanAssign(dain));
		assertTrue(scn.FreepsCanAssign(army));
		assertFalse(scn.FreepsCanAssign(elfarmy));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(dain));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(army));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(elfarmy));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dain, azog}, new PhysicalCardImpl[]{army, alfrid});
		assertTrue(scn.IsCharAssignedAgainst(dain, azog));
		assertTrue(scn.IsCharAssignedAgainst(army, alfrid));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TheAssignmentActionIsNotOfferedWhileThorinCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		var azog = scn.GetShadowCard("azog");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, crown, dain);
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);
		assertInZone(Zone.SHADOW_CHARACTERS, azog);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(crown));
		assertFalse(scn.FreepsCanAssign(dain));
	}

	@Test
	public void TheAssignmentActionIsNotOfferedWithNoDwarvenAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var nori = scn.GetFreepsCard("nori");
		var elfarmy = scn.GetFreepsCard("elfarmy");
		var azog = scn.GetShadowCard("azog");
		scn.MoveCompanionsToTable(nori);
		scn.MoveAlliesToTable(elfarmy);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, crown, elfarmy);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);
		assertInZone(Zone.SHADOW_CHARACTERS, azog);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(crown));
	}

	@Test
	public void TheAlliesMayBeAssignedOnlyUntilTheAssignmentPhaseEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var nori = scn.GetFreepsCard("nori");
		var dain = scn.GetFreepsCard("dain");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(nori);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, crown, dain);
		scn.FreepsPass();
		scn.MoveMinionsToTable(spider);
		assertInZone(Zone.SHADOW_CHARACTERS, spider);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsUseCardAction(crown);
		assertTrue(scn.FreepsCanAssign(dain));
		scn.PassCurrentPhaseActions();
		// Dain is left unassigned; Nori takes the Spider.
		scn.FreepsAssignToMinions(nori, spider);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(nori);
		scn.PassSkirmishActions();
		// The fierce Spider's second assignment phase: the first use is over.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(dain));
		assertTrue(scn.FreepsActionAvailable(crown));
	}
}
