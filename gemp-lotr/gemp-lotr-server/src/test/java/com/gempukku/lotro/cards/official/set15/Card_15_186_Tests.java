package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_186_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "15_186");

					// Morgul Hound: a [wraith] Orc, a [wraith] minion who is no Nazgûl; +4 and fierce only at 2 wounds on
					// the Ring-bearer
					put("hound", "7_192");
					// Ulaire Attea, Khamul of the East: the [raider] Nazgûl, a Nazgûl who is not [wraith]; his texts need
					// 3 and 5 burdens
					put("khamul", "103_61");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; answers only a weapon played on him
					// Úlairë Enquëa, Lieutenant of Morgul: another [wraith] Nazgûl, to be mounted; his maneuver ability is
					// never used
					put("enquea", "1_231");
					// Fell Beast: [wraith] mount, bearer must be a Nazgûl; bearer is fierce (Nelya is already), strength +2
					put("beast1", "6_83");
					put("beast2", "6_83");
					// Nazgûl Scimitar: [wraith] hand weapon on a [wraith] minion, a possession that is no mount; damage +1
					// only at 5 threats
					put("scimitar", "7_203");
					// Blade Tip: a [wraith] condition that any character may bear; its wound at the start of the
					// fellowship phase is never reached
					put("tip1", "1_209");
					put("tip2", "1_209");
					// Black Dart: a [wraith] condition of another title on an unbound companion; bearer cannot heal or
					// bear mounts
					put("dart", "8_69");
					// Wounded: an [isengard] condition on a Free Peoples Man, a Shadow condition of another culture;
					// wounds may not be removed from bearer
					put("wounded", "4_215");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Long I Fell
					put("gimli", "1_13");       // Gimli, Son of Glóin: the Dwarf who exerts for Baruk Khazâd; Skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Maneuver ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: vitality 3; a Skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a sixth companion; his abilities are never used
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, no companion; a Skirmish ability only
					put("baruk", "5_5");        // Baruk Khazâd: Maneuver event, "Exert a Dwarf to wound a minion"
					put("fell", "4_97");        // Long I Fell: Skirmish event; the Shadow player may make the Free Peoples player wound a minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNelyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Úlairë Nelya, Fell Rider
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>While you can spot 6 companions, [wraith] minions cannot take wounds (except during skirmish phases).<br><b>Skirmish:</b> If this minion is mounted, exert him twice to wound a companion bearing a [wraith] condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nelya");

		assertEquals("Úlairë Nelya", card.getBlueprint().getTitle());
		assertEquals("Fell Rider", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileSixCompanionsCanBeSpottedWraithMinionsCannotTakeWoundsOutsideSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var baruk = scn.GetFreepsCard("baruk");
		// With Frodo, six companions.
		scn.MoveCompanionsToTable(gandalf, gimli, aragorn, boromir, sam);
		scn.MoveCardsToHand(baruk);

		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, hound, khamul, chieftain);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(khamul));
		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Barred: Nelya herself and the Hound, [wraith] minions.  Offered: the [raider] Nazgûl and a [sauron] Orc.
		assertTrue(scn.FreepsHasCardChoiceAvailable(khamul, chieftain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nelya, hound));
		scn.FreepsChooseCard(khamul);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithFiveCompanionsAndAnAllyAWraithMinionTakesTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		var baruk = scn.GetFreepsCard("baruk");
		// With Frodo, five companions; the ally is the sixth character and no companion.
		scn.MoveCompanionsToTable(gandalf, gimli, aragorn, boromir);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(baruk);

		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, hound);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(nelya));
		scn.FreepsPlayCard(baruk);
		assertTrue(scn.FreepsHasCardChoiceAvailable(nelya, hound));
		scn.FreepsChooseCard(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DuringASkirmishPhaseAWraithMinionInNoSkirmishTakesAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(gandalf, gimli, aragorn, boromir, sam);
		scn.MoveCardsToHand(fell);

		var nelya = scn.GetShadowCard("nelya");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, chieftain}, new PhysicalCardImpl[]{gimli, nelya});

		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(0, scn.GetWoundsOn(nelya));
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();

		// Six companions, but Nelya waits for Gimli's skirmish during a skirmish phase: she may be wounded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(nelya, chieftain));
		scn.FreepsChooseCard(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MountedSkirmishAbilityExertsHimTwiceToWoundACompanionBearingAWraithCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn, boromir);
		scn.MoveAlliesToTable(bounder);

		var nelya = scn.GetShadowCard("nelya");
		var beast1 = scn.GetShadowCard("beast1");
		var tip1 = scn.GetShadowCard("tip1");
		var tip2 = scn.GetShadowCard("tip2");
		var dart = scn.GetShadowCard("dart");
		var wounded = scn.GetShadowCard("wounded");

		scn.StartGame();
		// Gimli, in Nelya's skirmish, and Boromir, exhausted and in no skirmish, bear [wraith] conditions of two titles;
		// the ally Bounder bears one too; Aragorn bears an [isengard] condition; Frodo bears none.
		scn.AttachCardsTo(gimli, tip1);
		scn.AttachCardsTo(boromir, dart);
		scn.AttachCardsTo(bounder, tip2);
		scn.AttachCardsTo(aragorn, wounded);
		scn.AddWoundsToChar(boromir, 2);
		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, beast1);
		assertAttachedTo(tip1, gimli);
		assertAttachedTo(dart, boromir);
		assertAttachedTo(tip2, bounder);
		assertAttachedTo(wounded, aragorn);
		assertAttachedTo(beast1, nelya);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, nelya);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowActionAvailable(nelya));
		scn.ShadowUseCardAction(nelya);
		assertEquals(2, scn.GetWoundsOn(nelya));

		// Either companion bearing a [wraith] condition, in the skirmish or not; not the ally who bears one, the
		// companion bearing an [isengard] condition, nor the Ring-bearer bearing none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bounder, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(boromir);

		// A wound, not an exertion: the exhausted Boromir is killed.
		assertInZone(Zone.DEAD, boromir);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotMountedItIsNotOfferedThoughAnotherNazgulIsMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var nelya = scn.GetShadowCard("nelya");
		var scimitar = scn.GetShadowCard("scimitar");
		var enquea = scn.GetShadowCard("enquea");
		var beast2 = scn.GetShadowCard("beast2");
		var tip1 = scn.GetShadowCard("tip1");

		scn.StartGame();
		scn.AttachCardsTo(gimli, tip1);
		scn.MoveMinionsToTable(nelya, enquea);
		// Nelya bears a possession that is no mount; Enquëa is the mounted Nazgûl.
		scn.AttachCardsTo(nelya, scimitar);
		scn.AttachCardsTo(enquea, beast2);
		assertAttachedTo(tip1, gimli);
		assertAttachedTo(scimitar, nelya);
		assertAttachedTo(beast2, enquea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, nelya}, new PhysicalCardImpl[]{aragorn, enquea});
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nelya));
	}

	@Test
	public void MountedWithTwoVitalityLeftItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var nelya = scn.GetShadowCard("nelya");
		var beast1 = scn.GetShadowCard("beast1");
		var tip1 = scn.GetShadowCard("tip1");

		scn.StartGame();
		scn.AttachCardsTo(gimli, tip1);
		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, beast1);
		scn.AddWoundsToChar(nelya, 1);
		assertAttachedTo(tip1, gimli);
		assertAttachedTo(beast1, nelya);
		assertEquals(1, scn.GetWoundsOn(nelya));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, nelya);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		// Vitality 3 with one wound: she cannot exert twice.
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nelya));
	}

	@Test
	public void WithNoCompanionBearingAWraithConditionTheExertionsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var nelya = scn.GetShadowCard("nelya");
		var beast1 = scn.GetShadowCard("beast1");
		var wounded = scn.GetShadowCard("wounded");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, wounded);
		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, beast1);
		assertAttachedTo(wounded, aragorn);
		assertAttachedTo(beast1, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, nelya);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowActionAvailable(nelya));
		scn.ShadowUseCardAction(nelya);

		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
