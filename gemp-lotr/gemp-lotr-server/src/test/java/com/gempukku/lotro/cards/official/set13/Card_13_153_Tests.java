package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_13_153_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coat", "13_153");

					// Ring-bound Hobbits of three titles, besides the Ring-bearer.
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound by rule, strength 3, vitality 4; a fellowship ability never used
					put("bilbo", "12_119");     // Bilbo, Melancholy Hobbit: Ring-bound; an optional trigger at the start of his own skirmishes only
					// Characters who are not Ring-bound Hobbits.
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound Hobbit companion; a skirmish ability never used
					put("faramir", "4_117");    // Faramir, Son of Denethor: a Ring-bound Man; his text is about skirmishes he is in
					put("bounder", "1_286");    // Bounder: a Hobbit ally (only companions are Ring-bound); a skirmish ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used

					// Damage +1 minions of two cultures and races, and a third for another skirmish.
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9; Damage +1 is its whole text
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll, strength 15; Damage +1 and fierce; its other text is about being played
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8; Damage +1 is its whole text
					put("battlecry", "1_128");  // Lurtz's Battle Cry: Skirmish event, "Make an Uruk-hai strength -1 and damage +1"

					// Fierce minions of two cultures and races, and a third for another skirmish.
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul, fierce, strength 8 +1 for each companion
					put("destroyer", "17_124"); // White Hand Destroyer: [uruk-hai] Uruk-hai, fierce, strength 10; hunter only for sites controlled
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					// Easterling Axeman: [raider] Man, strength 6, vitality 2; "While you can spot 2 burdens, this minion
					// is strength +3 and fierce."
					put("axeman1", "4_224");
					put("axeman2", "4_224");

					// A minion that comes into the bearer's skirmish after the ability is used.
					put("gollum", "7_59");      // Gollum, Vile Creature: [gollum] minion, strength 5; a skirmish ability used only on purpose
					put("controlled", "15_41"); // Controlled By The Ring: on Gollum, "Skirmish: If Gollum is not assigned to a skirmish, discard this condition to have him replace a minion skirmishing a companion"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MithrilcoatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Mithril-coat, Dwarf-mail
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Armor
		 * Game Text: Bearer must be a Ring-bound Hobbit.
		 * 	Each minion skirmishing bearer loses all damage bonuses.
		 * 	Skirmish: Exert bearer to make each minion he is skirmishing lose fierce and unable to gain fierce until
		 * 	the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("coat");

		assertEquals("Mithril-coat", card.getBlueprint().getTitle());
		assertEquals("Dwarf-mail", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnARingBoundHobbitAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var coat = scn.GetFreepsCard("coat");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		var faramir = scn.GetFreepsCard("faramir");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, bilbo, merry, faramir, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(coat);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(coat));
		scn.FreepsPlayCard(coat);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, faramir, bounder, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertAttachedTo(coat, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMinionSkirmishingBearerLosesAllDamageBonusesAndOneSkirmishingAnotherCompanionKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coat = scn.GetFreepsCard("coat");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetShadowCard("guard");
		var troll = scn.GetShadowCard("troll");
		var sentry = scn.GetShadowCard("sentry");
		var battlecry = scn.GetShadowCard("battlecry");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, coat);
		scn.MoveCardsToHand(battlecry);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(guard, troll, sentry);

		// The Guard and the Troll both go to Sam, the Troll put there by the Shadow player; the Sentry to Aragorn.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, guard}, new PhysicalCardImpl[]{aragorn, sentry});
		scn.ShadowAssignToMinions(sam, troll);

		// Assigned to the bearer but not yet skirmishing him: every bonus still there.
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		// In Aragorn's skirmish, fought first: the Sentry skirmishes another companion and keeps its bonus, and the
		// two waiting for Sam keep theirs.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, sentry));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		// In Sam's skirmish, both minions lose their bonus.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, guard));
		assertTrue(scn.IsCharSkirmishingAgainst(sam, troll));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		// "All": a bonus given during the skirmish is lost too, while the rest of the event lands.
		assertEquals(9, scn.GetStrength(guard));
		scn.FreepsPass();
		scn.ShadowPlayCard(battlecry);
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, sentry));
		scn.ShadowChooseCard(guard);
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsBearerToTakeFierceFromEachMinionSkirmishingHimAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var coat = scn.GetFreepsCard("coat");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetShadowCard("rider");
		var destroyer = scn.GetShadowCard("destroyer");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, coat);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(rider, destroyer, pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, rider}, new PhysicalCardImpl[]{aragorn, pursuer});
		scn.ShadowAssignToMinions(sam, destroyer);

		// Used in Aragorn's skirmish, fought first: the bearer pays, and nothing loses fierce, neither the Pursuer
		// skirmishing Aragorn nor the two minions waiting for Sam.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(destroyer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.FreepsActionAvailable(coat));
		scn.FreepsUseCardAction(coat);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(destroyer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.PassSkirmishActions();

		// Used in Sam's own skirmish: both minions skirmishing him lose fierce.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.FreepsActionAvailable(coat));
		scn.FreepsUseCardAction(coat);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(destroyer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var coat = scn.GetFreepsCard("coat");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, coat);

		scn.StartGame();
		// Sam, vitality 4, is exhausted; Frodo and Aragorn could exert.
		scn.AddWoundsToChar(sam, 3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(rider);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, rider);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(coat));
	}

	@Test
	public void MinionsSkirmishingBearerStayWithoutFierceAndCannotGainItUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coat = scn.GetFreepsCard("coat");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetShadowCard("rider");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, coat);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(rider, axeman1, axeman2);

		// The Rider and one Axeman go to Sam; the other Axeman to Aragorn.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, rider}, new PhysicalCardImpl[]{aragorn, axeman2});
		scn.ShadowAssignToMinions(sam, axeman1);

		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		scn.FreepsUseCardAction(coat);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));

		// After the ability, 2 burdens: each Axeman's own text would make him strength +3 and fierce.  The one
		// skirmishing Sam gains the strength and not fierce; the one waiting for Aragorn gains both.
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(axeman1));
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertEquals(9, scn.GetStrength(axeman2));
		assertTrue(scn.HasKeyword(axeman2, Keyword.FIERCE));

		// Sam (3) is overwhelmed by the Rider (8 + 3 companions) and the Axeman (9); Aragorn (8) loses to the other
		// Axeman (9).
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fierce assignment phase: only the Axeman who skirmished Aragorn is still fierce.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(axeman2, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(axeman2));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(rider));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(axeman1));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// From the regroup phase on, both are fierce again, the Axeman by his own text.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionThatComesIntoTheBearersSkirmishAfterTheUseKeepsFierceAndOneItReplacedStaysWithoutIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coat = scn.GetFreepsCard("coat");
		var sam = scn.GetFreepsCard("sam");
		var rider = scn.GetShadowCard("rider");
		var gollum = scn.GetShadowCard("gollum");
		var controlled = scn.GetShadowCard("controlled");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, coat);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(rider, gollum);
		scn.AttachCardsTo(gollum, controlled);
		// Fierce, as Emyn Muil 1_360 would make him, so that the loss of it can be told from the lack of it.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, gollum, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, rider);
		// Gollum is left over, and stays unassigned.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, rider));
		assertFalse(scn.IsCharAssigned(gollum));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(gollum, Keyword.FIERCE));
		scn.FreepsUseCardAction(coat);
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(gollum, Keyword.FIERCE));

		// Gollum takes the Rider's place in Sam's skirmish.
		assertTrue(scn.ShadowActionAvailable(controlled));
		scn.ShadowUseCardAction(controlled);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, gollum));
		assertFalse(scn.IsCharSkirmishing(rider));

		// The minion it reached was the one skirmishing Sam when it was used: Gollum, skirmishing him now, is still
		// fierce, and the Rider, out of the skirmish, is not fierce again until the regroup phase.
		assertTrue(scn.HasKeyword(gollum, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
