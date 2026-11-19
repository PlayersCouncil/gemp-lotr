package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_182_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("loathsome", "7_182");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12; fierce, and his texts read burdens only
					put("khamul", "103_61");
					// Black Steed: a mount borne by the Nazgul, strength +2 and +2 more at a plains (Ettenmoors, site 2)
					put("steed", "1_208");
					// Ulaire Nelya, Ringwraith in Twilight: a [wraith] Nazgul, strength 10; fierce and damage +1 only at 2
					// burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul, strength 9, vitality 2; her text is about
					// [wraith] conditions borne, and none is borne in her tests
					put("lemenya", "1_232");
					// Morgul Hound: a [wraith] minion who is no Nazgul (an Orc, strength 5, vitality 2); stronger only at 2
					// wounds on the Ring-bearer
					put("hound", "7_192");
					// Spider Poison: a Shadow possession a companion bears; "each time bearer is assigned to a skirmish,
					// bearer must exert"; its skirmish ability is never used
					put("poison", "8_28");
					// Black Breath: a [wraith] condition, borne here by a companion; its skirmish ability is never used
					put("breath", "1_207");

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; his skirmish ability is never used
					// Dwarven Armor: a possession, "Bearer must be a Dwarf"; bearer is not overwhelmed unless tripled
					put("armor", "1_8");
					// Ring of Artifice: an artifact for a Dwarf (damage +1; strength +0 with no [dwarven] artifact in the
					// discard pile); its regroup ability is never used
					put("ring", "17_4");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("sword", "1_112");      // Ranger's Sword: strength +2 and damage +1, on Aragorn
					// Thrarin, Dwarven Smith: a Dwarf ally; "Maneuver: Exert Thrarin to allow him to participate in archery
					// fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Long I Fell
					// Long I Fell: "Spot Gandalf to prevent all wounds to him. Any Shadow player may make you wound a minion
					// to prevent this."
					put("fell", "4_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("loathsome", "7_182");
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl, strength 9, vitality 2; her text is about [wraith] conditions
					put("runner", "1_178");     // Goblin Runner: the minion of a skirmish that has not begun
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("spear", "7_108");      // Knight's Spear: a hand weapon, strength +2; a skirmish ability used only on purpose
					put("armor", "7_93");       // Footman's Armor: a response only when its bearer is about to be overwhelmed
					put("saga", "1_114");       // The Saga of Elendil: a condition he bears, and no possession
					put("gimli", "1_13");       // Gimli, Son of Glóin: the companion of the other skirmish
					put("dwarven", "1_8");      // Dwarven Armor: the possession he bears
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LoathsomeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Loathsome
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a skirmish that involved a Nazgûl is about to end, discard a possession borne by a companion in that skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("loathsome");

		assertEquals("Loathsome", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedAsASkirmishInvolvingANazgulIsAboutToEndDiscardsAPossessionBorneByACompanionInIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var loathsome = scn.GetShadowCard("loathsome");
		var lemenya = scn.GetShadowCard("lemenya");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		var armor = scn.GetFreepsCard("armor");
		var saga = scn.GetFreepsCard("saga");
		var gimli = scn.GetFreepsCard("gimli");
		var dwarven = scn.GetFreepsCard("dwarven");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, spear, armor, saga);
		scn.AttachCardsTo(gimli, dwarven);
		scn.MoveCardsToHand(loathsome);
		assertAttachedTo(spear, aragorn);
		assertAttachedTo(armor, aragorn);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(dwarven, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gimli, runner});
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's 8 and the Spear's 2 against 9: it is the Spear that wins him the skirmish.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(lemenya));
		// While the skirmish is being fought the event is not offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(loathsome));
		scn.ShadowPass();

		// The skirmish is resolved and Lemenya has taken the loser's wound: now it is about to end.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(loathsome));

		scn.ShadowPlayCard(loathsome);
		// The two possessions Aragorn bears; not the condition he bears, nor the armor of Gimli, who is assigned
		// to a skirmish that has not begun.
		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, armor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saga, dwarven));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(spear);

		assertInZone(Zone.DISCARD, spear, loathsome);
		assertAttachedTo(armor, aragorn);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(dwarven, gimli);
		// The Spear that won the skirmish is gone, and the result stands (CR 4.2 on Loathsome: the discard does
		// not affect the resolution of that skirmish).
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NotOfferedAsASkirmishWithNoNazgulEndsAndOfferedAsAKhamulsSkirmishEndsForAPossessionOfEitherSideBorneByTheCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loathsome = scn.GetShadowCard("loathsome");
		var khamul = scn.GetShadowCard("khamul");
		var steed = scn.GetShadowCard("steed");
		var hound = scn.GetShadowCard("hound");
		var poison = scn.GetShadowCard("poison");
		var breath = scn.GetShadowCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(gimli, armor, ring, poison, breath);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToHand(loathsome);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(poison, gimli);
		assertAttachedTo(breath, gimli);
		assertAttachedTo(sword, aragorn);
		assertInHand(loathsome);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, hound);
		scn.AttachCardsTo(khamul, steed);
		assertAttachedTo(steed, khamul);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, hound}, new PhysicalCardImpl[]{gimli, khamul});
		// Spider Poison: Gimli exerts as he is assigned.
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.SetTwilight(3);

		// Aragorn's skirmish against a [wraith] minion who is no Nazgul: it ends with no offer, and the game goes
		// straight on to the choice of the next skirmish, where an offer to the Shadow player would have come first.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(hound);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertInHand(loathsome);
		assertAttachedTo(sword, aragorn);

		// Khamul (12, +2 for the Steed, +2 at a plains) beats Gimli (6), who is not overwhelmed short of triple.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(16, scn.GetStrength(khamul));
		assertEquals(6, scn.GetStrength(gimli));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));

		assertTrue(scn.ShadowPlayAvailable(loathsome));
		scn.ShadowPlayCard(loathsome);
		// The possessions Gimli bears, a Free Peoples one and a Shadow one; not his artifact or his condition, the
		// minion's mount, or the Sword of Aragorn, whose skirmish is over.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring, breath, steed, sword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(poison);

		assertInDiscard(poison, loathsome);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(breath, gimli);
		assertAttachedTo(steed, khamul);
		assertAttachedTo(sword, aragorn);
		// Khamul is fierce: the fierce round follows.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithAnAllySkirmishingTheNazgulItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loathsome = scn.GetShadowCard("loathsome");
		var nelya = scn.GetShadowCard("nelya");
		var thrarin = scn.GetFreepsCard("thrarin");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, armor);
		scn.MoveCardsToHand(loathsome);
		assertAttachedTo(armor, thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		// So that Thrarin wins and lives, and does not overwhelm: 4 + 8 against 10.
		scn.ApplyAdHocModifier(new StrengthModifier(null, thrarin, null, 8));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, nelya);
		scn.SetTwilight(3);

		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(nelya));

		// An event's target is no requirement: it is offered, and played for nothing.  The ally is no companion.
		assertTrue(scn.ShadowPlayAvailable(loathsome));
		scn.ShadowPlayCard(loathsome);

		assertInDiscard(loathsome);
		assertAttachedTo(armor, thrarin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASkirmishWhoseNazgulIsKilledBeforeItIsResolvedStillInvolvedANazgulAsItEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loathsome = scn.GetShadowCard("loathsome");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToHand(loathsome, fell);
		assertAttachedTo(sword, aragorn);
		assertInHand(loathsome, fell);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		// One wound from death.
		scn.AddWoundsToChar(lemenya, 1);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(aragorn);

		// Long I Fell: the Shadow player has the Free Peoples player wound a minion, and it is Lemenya, the skirmish's
		// only minion.  She is killed and the skirmish ends before it is resolved.
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		scn.FreepsChooseCard(lemenya);
		assertInDiscard(lemenya);

		// It involved a Nazgul all the same (CR 4.2 on Loathsome: "even if that skirmish ends before resolution").
		assertTrue(scn.ShadowPlayAvailable(loathsome));
		scn.ShadowPlayCard(loathsome);

		assertInDiscard(sword, loathsome);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
