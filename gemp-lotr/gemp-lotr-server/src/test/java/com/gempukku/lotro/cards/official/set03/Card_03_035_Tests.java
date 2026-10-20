package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_035_Tests
{

	// The Ring-bearer is Frodo, Son of Drogo, who has the Frodo signet.  No archer is laid, so that the archery
	// phase places no wounds on the way to the skirmishes.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trust1", "3_35");
					put("trust2", "3_35");
					// Gandalf, Friend of the Shirefolk: the Gandalf who exerts; he has the Frodo signet, not the Gandalf one
					put("gandalf", "1_72");
					// Ent Avenger: a [gandalf] companion who is not Gandalf, vitality 4
					put("avenger", "6_27");

					// Gandalf signet: Gimli 1_13 (strength 6; a skirmish ability on himself, not used),
					// Aragorn 1_89 (strength 8), Pippin 1_307 and Merry 2_104 (strength 3, texts not reached here)
					put("gimli", "1_13");
					put("aragorn", "1_89");
					put("pippin", "1_307");
					put("merry", "2_104");
					// Frodo signet: Merry, Friend to Sam 1_302 (strength 3)
					put("merryf", "1_302");
					// Aragorn signet: Boromir, Lord of Gondor 1_96 (strength 7)
					put("boromir", "1_96");
					// No signet: Haldir, Elf of the Golden Wood 1_48 (strength 5; +2 only at sites 6 to 8)
					put("haldir", "1_48");
					// An ally: Barliman Butterbur 1_70 (strength 1; a fellowship ability)
					put("barliman", "1_70");

					// Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("savage1", "1_151");
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrustMeasYouOnceDidStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Trust Me as You Once Did
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Game Text: Plays to your support area.
		 * <b>Skirmish:</b> Exert Gandalf to make a companion strength +1 for each companion with the Gandalf signet you can spot (limit +3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trust1");

		assertEquals("Trust Me as You Once Did", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishActionExertsGandalfToMakeACompanionStrongerByEachGandalfSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merryf = scn.GetFreepsCard("merryf");
		var boromir = scn.GetFreepsCard("boromir");
		var haldir = scn.GetFreepsCard("haldir");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		// Two companions with the Gandalf signet (Gimli, Aragorn); three with the Frodo signet (Gandalf, Frodo,
		// Merry); one with the Aragorn signet (Boromir); one with none (Haldir); seven companions in all.
		scn.MoveCompanionsToTable(gandalf, gimli, aragorn, merryf, boromir, haldir);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(trust);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(trust));

		scn.FreepsUseCardAction(trust);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// Any companion, in the skirmish or not, whatever his signet; not an ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, aragorn, boromir, haldir, gandalf, frodo, merryf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// 6 + 2: two Gandalf signets.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithFourGandalfSignetsTheBonusIsThreeAndTheLimitIsThenReached() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(gandalf, gimli, aragorn, pippin, merry);
		scn.MoveCardsToSupportArea(trust);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));

		// Aragorn is in no skirmish, and may be chosen.  Four Gandalf signets: +3, the limit, not +4.
		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(11, scn.GetStrength(aragorn));
		scn.ShadowPass();

		// At the limit: still offered (it has a cost), Gandalf exerts, and nothing more is added.
		assertTrue(scn.FreepsActionAvailable(trust));
		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(gimli);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneGandalfSignetItGivesPlus1ToThreeCompanionsInThreeActions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var haldir = scn.GetFreepsCard("haldir");
		// Gimli is the one companion with the Gandalf signet.
		scn.MoveCompanionsToTable(gandalf, gimli, boromir, haldir);
		scn.MoveCardsToSupportArea(trust);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(5, scn.GetStrength(haldir));

		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(gimli);
		assertEquals(7, scn.GetStrength(gimli));
		scn.ShadowPass();

		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(boromir);
		assertEquals(8, scn.GetStrength(boromir));
		scn.ShadowPass();

		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(haldir);
		assertEquals(6, scn.GetStrength(haldir));

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithTwoGandalfSignetsItGivesPlus2AndThenOnlyPlus1ToAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gandalf, gimli, aragorn, boromir);
		scn.MoveCardsToSupportArea(trust);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(gimli);
		assertEquals(8, scn.GetStrength(gimli));
		scn.ShadowPass();

		// The limit is the card's for the phase, not each companion's: "the last +1 is ignored".
		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(boromir);
		assertEquals(8, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(gimli));
		scn.ShadowPass();

		// +3 in all: a third use pays its cost and adds nothing.
		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ThePlus3IsGoneInTheNextSkirmishWhereItCanBeUsedForPlus3Again() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(gandalf, gimli, aragorn, pippin);
		scn.MoveCardsToSupportArea(trust);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage1}, new PhysicalCardImpl[]{aragorn, savage2});
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(6, scn.GetStrength(gimli));

		// Three Gandalf signets: +3.
		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(gimli);
		assertEquals(9, scn.GetStrength(gimli));
		// Still in force at the next decision of the same skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		assertEquals(9, scn.GetStrength(gimli));
		scn.FreepsPass();

		// Each skirmish is a phase of its own: in the next one the +3 is gone, and the limit is new.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TwoCopiesGivePlus6InOneSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust1 = scn.GetFreepsCard("trust1");
		var trust2 = scn.GetFreepsCard("trust2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(gandalf, gimli, aragorn, pippin);
		scn.MoveCardsToSupportArea(trust1, trust2);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsUseCardAction(trust1);
		scn.FreepsChooseCard(gimli);
		assertEquals(9, scn.GetStrength(gimli));
		scn.ShadowPass();

		// The limit is each copy's own.
		assertTrue(scn.FreepsActionAvailable(trust2));
		scn.FreepsUseCardAction(trust2);
		scn.FreepsChooseCard(gimli);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(12, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhaseAndIsInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.MoveCardsToSupportArea(trust);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(trust));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(trust));
	}

	@Test
	public void IsNotOfferedWhenGandalfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.MoveCardsToSupportArea(trust);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(trust));
	}

	@Test
	public void IsNotOfferedWithAGandalfCompanionWhoIsNotGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust1");
		var avenger = scn.GetFreepsCard("avenger");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(avenger, gimli);
		scn.MoveCardsToSupportArea(trust);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		// Ent Avenger, a [gandalf] companion, could exert; he is not Gandalf.
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(trust));
	}
}
