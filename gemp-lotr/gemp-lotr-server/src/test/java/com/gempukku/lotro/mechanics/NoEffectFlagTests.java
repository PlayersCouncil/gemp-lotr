package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * The no-effect flag.
 *
 * Comprehensive Rules 4.2, "effect": "If you meet all the requirements and pay all the costs for playing a card,
 * you may play that card even if the card will have no effect."  So an event, and an ability with a cost, is
 * offered whenever its requirements are met and its costs can be paid (Ketura, 2026-10-06, on Q-0034).
 *
 * Ketura, 2026-10-06: "On decisions sent to a player, let's add a flag of 'no effect', meaning the action is
 * determined to have no effect ahead of time.  In addition, for skirmish actions only I want that flag to be able
 * to detect that it will have no effect on a currently-skirmishing character, for the extremely common case of 'I
 * used my hobbit pump on a gondor skirmisher'. ... such actions highlight with a different color, which will
 * permit players to make such actions should they deem it strategically necessary."  And of skirmish actions:
 * "The main thing I want to avoid is pumping strength or canceling skirmishes of people who aren't actually
 * skirmishing ... If it actually results in something hitting the table, it's fine to leave noeffect off of it."
 *
 * Each action in a choice of actions carries one word: "false", or why it will do nothing: "effect" (its effect
 * will do nothing), "limit" (what the ability gives has reached its limit) or "skirmish" (a skirmish action that
 * reaches nobody in the skirmish being fought).  A flagged action is offered and may be taken all the same.
 *
 * Ketura, 2026-10-07, on Q-0082 and Q-0093: "there's a nuance between 'limit +3' and 'limit once', with the former
 * being a cumulative addition of multiple invocations, and the latter being a limit on the number of times the
 * ability can invoke at all"; "the first kind should be offered but effectless, while the second should halt being
 * offered once the invocation limit is reached."  And: "Dark Tidings is more like '+1' than 'once'; it is limiting
 * the number of threats that can be added the way that Scimitar limits the strength bonus."
 */
public class NoEffectFlagTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gimli, Son of Gloin: strength 6, vitality 3.  "Skirmish: Exert Gimli to make him strength +2."
					put("gimli", "1_13");
					// Stout and Sturdy: a [shire] event, cost 1.  "Maneuver or Skirmish: Heal a Hobbit."
					put("stout", "1_315");
					put("stout2", "1_315");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit, strength 3, vitality 4

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected HashMap<String, String> EffectCards() {
		return new HashMap<>()
		{{
			put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3
			// Boromir's Cloak: "Bearer must be Boromir.  Maneuver: Exert Boromir to discard a weather condition."
			put("cloak", "1_98");
			// Saruman's Chill: an [isengard] weather condition that plays on a site.  "Each Hobbit who
			// moves from this site must exert.  Discard this condition at the end of the turn."
			put("chill", "1_134");
			// Devilry of Orthanc: an [isengard] condition that is no weather.  Its abilities need an
			// Uruk-hai to exert in the Shadow phase, or 8 tokens.
			put("devilry", "5_49");

			// Celeborn, Lord of Lorien: an [elven] ally, vitality 3.  "Fellowship: Exert Celeborn to heal
			// an [elven] ally."
			put("celeborn", "1_34");
			put("orophin", "1_56");     // Orophin, Lorien Bowman: an [elven] ally; an archery ability
			// Uruk Shaman: an Uruk-hai, strength 8, vitality 2.  "Maneuver: Remove (2) to heal an Uruk-hai."
			put("shaman", "1_152");

			put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
			put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion, strength 3, vitality 4
			// Cliffs of Emyn Muil: a [shire] condition.  "Skirmish: Discard this condition to wound a
			// minion a Hobbit companion is skirmishing."  Its other text is about site 1 of another block.
			put("cliffs", "4_299");

			// Ulaire Attea, Keeper of Dol Guldur: strength 12, vitality 3, fierce.  "Skirmish: Remove (1) to
			// make Ulaire Attea strength +1 (limit +5)."
			put("attea", "1_229");

			put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
			put("runner2", "1_178");
		}};
	}

	protected VirtualTableScenario GetEffectScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(EffectCards(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected HashMap<String, String> SkirmishCards() {
		return new HashMap<>()
		{{
			// Boromir, Son of Denethor: strength 7, vitality 3.  "Skirmish: Exert Boromir to make a Hobbit
			// strength +3."
			put("boromir", "1_97");
			// Gimli, Son of Gloin: strength 6, vitality 3.  "Skirmish: Exert Gimli to make him strength +2."
			put("gimli", "1_13");
			put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
			put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit, strength 3, vitality 4
			// Golradir, Councilor of Imladris: an [elven] ally, vitality 3.  "Skirmish: Exert Golradir to
			// make an Orc strength -1."
			put("golradir", "3_20");
			// Bounder: a [shire] ally, a Hobbit, vitality 2.  "Skirmish: Exert this ally to prevent a Hobbit
			// from being overwhelmed unless that Hobbit's strength is tripled."
			put("bounder", "1_286");
			put("bounder2", "1_286");
			// Arwen, Daughter of Elrond: strength 6, vitality 3; stronger while skirmishing a Nazgul.
			put("arwen", "1_30");
			// Asfaloth, Elven Steed: strength +2.  "Bearer must be an Elf.  Skirmish: If bearer is Arwen,
			// exert her to make each minion skirmishing her strength -2."
			put("asfaloth", "7_17");
			// Durability: a [dwarven] event, cost 0.  "Skirmish: Make a Dwarf strength +2.  Then, if that
			// Dwarf is at a battleground or mountain site, draw a card."
			put("durability", "12_4");
			put("durability2", "12_4");
			// Henneth Annun: a [gondor] condition.  "Skirmish: Spot a [gondor] Man and discard this
			// condition to wound a roaming minion."  Its other text is about site 6 of another block.
			put("henneth", "4_125");
			put("henneth2", "4_125");
			// Warmed Up a Bit: a [shire] event, cost 0.  "Skirmish: Discard an unbound Hobbit."
			put("warmed", "4_322");
			put("warmed2", "4_322");
			// Merry, Friend to Sam: an unbound Hobbit.  His skirmish ability adds his strength to another
			// companion, and is not used.
			put("merry", "1_302");

			put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5, vitality 1, site 4
			put("runner2", "1_178");
			put("savage", "1_151");     // Uruk Savage: an Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
			// Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text.
			put("troop", "1_177");
			// Moria Axe: strength +2, damage +1.  "Bearer must be a [moria] Orc.  Skirmish: Exert bearer to
			// make him strength +2."
			put("axe", "1_190");
			// Goblin Man: an [isengard] Orc, strength 6, vitality 2, site 2, so not roaming at site 2.  It is
			// discarded if underground; its ability is a Shadow one.
			put("goblinman", "2_42");
			// Armored Easterling: strength 10, vitality 3.  Three skirmish abilities, each "Remove (2) to
			// make a lurker ...": strength +1; fierce until the regroup phase; damage +1.
			put("easterling", "11_68");
			// Emboldened Orc: a lurker, strength 11, vitality 2, stronger for each companion who is not
			// assigned to a skirmish.  It has no ability to use.
			put("emboldened", "11_119");
		}};
	}

	// Site 2 of the Fellowship block is Ettenmoors: "Skirmish: Exert your companion or minion to make that
	// character strength +2."  Each player is offered it in every skirmish there.
	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(SkirmishCards(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A site path that is not the Fellowship block's, so that Hobbit Stealth's "at any other site" is the
	// clause in force.  Site 2 is King's Tent, whose ability is a Shadow one.
	protected VirtualTableScenario GetOtherSitesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(CancelCards(),
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected HashMap<String, String> CancelCards() {
		return new HashMap<>()
		{{
			// Hobbit Stealth: a [shire] event, cost 1.  "Skirmish: At sites 1 to 5, cancel a skirmish
			// involving a Hobbit.  At any other site, make a Hobbit strength +2."
			put("stealth", "1_298");
			put("stealth2", "1_298");
			put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit, strength 3, vitality 4
			put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4

			// Gandalf, Friend of the Shirefolk: vitality 4; stronger for each race in the fellowship.
			put("gandalf", "1_72");
			// Gandalf's Staff: "Bearer must be Gandalf. ... Skirmish: Exert Gandalf twice to cancel a
			// skirmish involving him."
			put("staff", "2_22");

			put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
			put("runner2", "1_178");
		}};
	}

	// The default format, in which a skirmish involving the Ring-bearer may not be cancelled.
	protected VirtualTableScenario GetCancelScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(CancelCards(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A format in which a skirmish involving the Ring-bearer may be cancelled.
	protected VirtualTableScenario GetFellowshipBlockCancelScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(CancelCards(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	protected HashMap<String, String> LimitCards() {
		return new HashMap<>()
		{{
			// Dark Tidings: a [raider] condition.  "Regroup: Spot 2 [raider] Men and remove (3) to add a
			// threat (limit 1)."
			put("tidings", "7_130");
			// Easterling Axeman: a [raider] Man, strength 6, vitality 2; stronger and fierce at 2 burdens.
			put("axeman", "4_224");
			put("axeman2", "4_224");
			// More to My Liking: a [dwarven] condition.  "Maneuver: Stack the top card from your draw deck
			// here (limit once per phase)."  Its other ability needs a Dwarf to spot.
			put("liking", "5_9");
			// Uruk Slayer: strength 9, vitality 2.  "Skirmish: Remove (1) to make this minion strength +1
			// (limit +3)."
			put("slayer", "1_153");
			// Realm of Dwarrowdelf: a [dwarven] condition.  "Skirmish: Discard the top 3 cards from your
			// draw deck to make a Dwarf strength +1 (limit +3) and, if underground, damage +1 (limit +3)."
			put("realm", "2_12");
			put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, strength 6, vitality 3
			put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
			// Anduril, King's Blade, as the Players' Council has it: "Bearer must be Aragorn. ...
			// Assignment: Add a threat to make Aragorn defender +1 (limit +1)."
			put("anduril", "57_80");
			// Shadowplay: a [shire] condition.  "Each time a minion is played, you may exert a Hobbit
			// companion to exert that minion (limit once per turn)."
			put("shadowplay", "10_114");
			put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion, vitality 4
			// Sam, Son of Hamfast, as the Players' Council has him: vitality 4.  "Fellowship: Exert Sam to
			// take a [shire] item into hand from your draw deck (limit once per turn)."
			put("gardener", "51_310");
			put("sword", "1_299");      // Hobbit Sword: a [shire] possession
			put("pipe", "1_285");       // Bilbo's Pipe: a [shire] possession

			put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1, cost 1
			put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, cost 6, no text
			put("troop2", "1_177");
			// Cards for the draw deck: Realm of Dwarrowdelf discards three of them at each use.
			put("deck1", "1_178");
			put("deck2", "1_178");
			put("deck3", "1_178");
			put("deck4", "1_178");
		}};
	}

	protected VirtualTableScenario GetLimitScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(LimitCards(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EveryOfferedActionCarriesTheFlagAndAUsefulOneIsNotFlagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(stout);
		scn.AddWoundsToChar(frodo, 1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);

		assertTrue(scn.IsCharSkirmishingAgainst(gimli, runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Three actions are on offer, the third being the site's (Ettenmoors, site 2: "Skirmish: Exert your
		// companion or minion to make that character strength +2").  The decision says of each whether it would
		// do nothing: the reading fails where the decision does not carry one word for each action it offers.
		assertEquals(3, scn.FreepsGetAvailableActions().size());
		// Gimli is the one fighting, Frodo has a wound to heal, and the site strengthens whoever is exerted
		// for it: each action does something.
		assertEquals("false", scn.FreepsActionNoEffect(gimli));
		assertEquals("false", scn.FreepsActionNoEffect(stout));
		assertEquals("false", scn.FreepsActionNoEffect(scn.GetCurrentSite()));

		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ADudEventIsOfferedAndFlagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var stout = scn.GetFreepsCard("stout");
		var stout2 = scn.GetFreepsCard("stout2");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(stout, stout2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		// No Hobbit has a wound to heal.  An event is offered whatever it will do, and is marked.
		assertTrue(scn.FreepsPlayAvailable(stout));
		assertEquals("effect", scn.FreepsActionNoEffect(stout));
		assertEquals("effect", scn.FreepsActionNoEffect(stout2));

		// It may be played all the same: it is paid for and spent, and heals nobody.
		var twilight = scn.GetTwilight();
		scn.FreepsPlayCard(stout);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, stout);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// A Hobbit takes a wound.  He is not in the skirmish, and a heal does not have to be.
		scn.AddWoundsToChar(sam, 1);
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.IsCharSkirmishing(sam));
		assertEquals("false", scn.FreepsActionNoEffect(stout2));

		scn.FreepsPlayCard(stout2);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInZone(Zone.DISCARD, stout2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAbilityWhoseCostCanBePaidAndWhoseEffectHasNoTargetIsFlagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEffectScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, cloak);

		var chill = scn.GetShadowCard("chill");
		var devilry = scn.GetShadowCard("devilry");
		var runner = scn.GetShadowCard("runner");
		// A Shadow condition that is not a weather condition.
		scn.MoveCardsToSupportArea(devilry);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// The cost can be paid, so the ability is offered; with no weather condition it is marked.
		assertTrue(scn.FreepsActionAvailable(cloak));
		assertEquals("effect", scn.FreepsActionNoEffect(cloak));

		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsUseCardAction(cloak);
		// The cost is paid and nothing is discarded.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SUPPORT, devilry);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// With a weather condition on the site, the same ability is not marked.
		scn.AttachCardsTo(scn.GetCurrentSite(), chill);
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals("false", scn.FreepsActionNoEffect(cloak));

		scn.FreepsUseCardAction(cloak);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInZone(Zone.DISCARD, chill);
		assertInZone(Zone.SUPPORT, devilry);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACostThatCouldMakeTheTargetLeavesTheActionUnflagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEffectScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(celeborn, orophin);

		var shaman = scn.GetShadowCard("shaman");

		scn.StartGame();

		// No [elven] ally has a wound to heal.  But the cost is an exertion, and an exertion makes one:
		// what is true of the board now is not what the heal will find, so nothing is said.
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertTrue(scn.FreepsActionAvailable(celeborn));
		assertEquals("false", scn.FreepsActionNoEffect(celeborn));

		scn.MarkGameLog();
		scn.FreepsUseCardAction(celeborn);
		// Celeborn exerted and, being the one [elven] ally with a wound, was healed of it: left with the
		// exertion he would have 1.
		assertTrue(scn.GameLogSinceMarkMentions(celeborn));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A heal bought with twilight has no such doubt: with no Uruk-hai wounded, it is marked.
		scn.MoveMinionsToTable(shaman);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertEquals("effect", scn.ShadowActionNoEffect(shaman));
	}

	@Test
	public void AWoundOfSomebodyInAnotherCharactersSkirmishIsFlaggedOnlyWhileThereIsNobodyToWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEffectScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var cliffs = scn.GetFreepsCard("cliffs");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToSupportArea(cliffs);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, runner},
				new PhysicalCardImpl[]{aragorn, runner2});

		// In the Hobbit's skirmish there is a minion a Hobbit companion is skirmishing.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner));
		assertEquals("false", scn.FreepsActionNoEffect(cliffs));

		// Sam loses, 3 against 5, and takes a wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));

		// In the Man's skirmish there is none: nobody at all for the wound, though a minion is fighting.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner2));
		assertTrue(scn.FreepsActionAvailable(cliffs));
		assertEquals("effect", scn.FreepsActionNoEffect(cliffs));

		scn.FreepsUseCardAction(cliffs);
		assertInZone(Zone.DISCARD, cliffs);
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACancelWithNobodyOfItsKindInTheSkirmishBeingFoughtIsFlagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCancelScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(gandalf, staff);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{gandalf, runner2});
		scn.FreepsResolveSkirmish(aragorn);

		// Gandalf's skirmish is still to come; the one being fought does not involve him.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(gandalf, runner2));
		assertTrue(scn.FreepsActionAvailable(staff));
		assertEquals("effect", scn.FreepsActionNoEffect(staff));

		scn.FreepsUseCardAction(staff);
		// The cost is paid and nothing is cancelled: not this skirmish, and not his own.
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(gandalf, runner2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn wins, 8 against 5.  Gandalf is healed by the rig so that he can exert twice again.
		scn.PassSkirmishActions();
		scn.RemoveWoundsFromChar(gandalf, 2);
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, runner2));
		assertEquals("false", scn.FreepsActionNoEffect(staff));

		scn.FreepsUseCardAction(staff);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertFalse(scn.IsCharSkirmishing(gandalf));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void HobbitStealthAtSites1To5IsFlaggedWhileNoHobbitIsInTheSkirmishBeingFought() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCancelScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var stealth = scn.GetFreepsCard("stealth");
		var stealth2 = scn.GetFreepsCard("stealth2");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(stealth, stealth2);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{sam, runner2});
		scn.FreepsResolveSkirmish(aragorn);

		// Site 2 of the Fellowship block: the event cancels, and does not strengthen anybody.  A Hobbit is
		// assigned to a skirmish that has not begun; none is in the one being fought.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(sam, runner2));
		assertEquals("effect", scn.FreepsActionNoEffect(stealth));

		var twilight = scn.GetTwilight();
		scn.FreepsPlayCard(stealth);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, stealth);
		// Nothing is cancelled, and nobody is stronger: Sam is 3 and Frodo 3 + 1 for the Ring.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(sam, runner2));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(scn.GetRingBearer()));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn wins, 8 against 5.  In the Hobbit's own skirmish the event is not marked, and cancels it.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner2));
		assertEquals("false", scn.FreepsActionNoEffect(stealth2));

		scn.FreepsPlayCard(stealth2);
		assertFalse(scn.IsCharSkirmishing(sam));
		assertFalse(scn.IsCharAssigned(sam));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACancelOfTheRingBearersSkirmishIsFlaggedInAFormatThatForbidsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCancelScenario();

		var frodo = scn.GetRingBearer();
		var stealth = scn.GetFreepsCard("stealth");
		scn.MoveCardsToHand(stealth);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		// A Hobbit is in the skirmish, at site 2; it is the format that keeps his skirmish from being cancelled.
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner));
		assertEquals("effect", scn.FreepsActionNoEffect(stealth));

		scn.FreepsPlayCard(stealth);
		assertInZone(Zone.DISCARD, stealth);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACancelOfTheRingBearersSkirmishIsNotFlaggedInAFormatThatAllowsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockCancelScenario();

		var frodo = scn.GetRingBearer();
		var stealth = scn.GetFreepsCard("stealth");
		scn.MoveCardsToHand(stealth);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner));
		assertEquals("false", scn.FreepsActionNoEffect(stealth));

		scn.FreepsPlayCard(stealth);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABonusCountedAgainstALimitInsideItsEffectIsFlaggedOnceTheLimitIsReached() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEffectScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var attea = scn.GetShadowCard("attea");

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, attea);
		scn.FreepsPass();

		assertEquals(12, scn.GetStrength(attea));
		// Five uses, each +1, none of them marked.
		for (int used = 0; used < 5; used++) {
			assertEquals("false", scn.ShadowActionNoEffect(attea));
			scn.ShadowUseCardAction(attea);
			assertEquals(12 + used + 1, scn.GetStrength(attea));
			scn.FreepsPass();
		}

		// At the limit the ability is still offered, its cost being payable, and is marked as at its limit.
		var twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(attea));
		assertEquals("limit", scn.ShadowActionNoEffect(attea));

		scn.ShadowUseCardAction(attea);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(17, scn.GetStrength(attea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AHobbitPumpIsFlaggedWhileNoHobbitIsInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(boromir, sam);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, runner},
				new PhysicalCardImpl[]{frodo, runner2});
		scn.FreepsResolveSkirmish(boromir);

		// Boromir is fighting.  Of the two Hobbits one is assigned to a skirmish that has not begun and the
		// other to none: the +3 can reach neither side of this skirmish.
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner));
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner2));
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.FreepsActionAvailable(boromir));
		assertEquals("skirmish", scn.FreepsActionNoEffect(boromir));

		// It may be used all the same, and does what it says.
		assertEquals(3, scn.GetStrength(sam));
		scn.FreepsUseCardAction(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam));
		scn.FreepsChooseCard(sam);
		assertEquals(3 + 3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Boromir wins, 7 against 5, and the skirmish is over: so is Sam's +3.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner2));
		assertEquals(3, scn.GetStrength(sam));

		// A Hobbit is in the skirmish now.  The ability is not marked, though a Hobbit who is not fighting
		// could be the one chosen.
		assertEquals("false", scn.FreepsActionNoEffect(boromir));
		scn.FreepsUseCardAction(boromir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam));
		scn.FreepsChooseCard(frodo);
		// 3 + 1 for the Ring + 3.
		assertEquals(3 + 1 + 3, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void APumpOfItselfOrItsBearerIsFlaggedWhileThatCharacterIsNotFighting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var axe = scn.GetShadowCard("axe");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.AttachCardsTo(troop, axe);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{gimli, troop});
		scn.FreepsResolveSkirmish(aragorn);

		// Gimli and the axe's bearer are assigned to each other, and their skirmish has not begun.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(gimli, troop));
		assertEquals("skirmish", scn.FreepsActionNoEffect(gimli));

		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(6 + 2, scn.GetStrength(gimli));

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals("skirmish", scn.ShadowActionNoEffect(axe));
		// 13 + 2 for the axe.
		assertEquals(15, scn.GetStrength(troop));
		scn.ShadowUseCardAction(axe);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(15 + 2, scn.GetStrength(troop));

		// Aragorn wins, 8 against 5.  The skirmish the two bonuses were given in is over, and they with it.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, troop));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(15, scn.GetStrength(troop));

		// In their own skirmish neither ability is marked.
		assertEquals("false", scn.FreepsActionNoEffect(gimli));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals("false", scn.ShadowActionNoEffect(axe));
	}

	@Test
	public void ABonusThatOutlastsTheSkirmishIsNotFlagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var easterling = scn.GetShadowCard("easterling");
		var emboldened = scn.GetShadowCard("emboldened");

		scn.StartGame();
		scn.MoveMinionsToTable(easterling, emboldened);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, easterling);
		scn.FreepsPass();

		// The one lurker in play is not in the skirmish, nor assigned to one.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, easterling));
		assertFalse(scn.IsCharAssigned(emboldened));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// Strength and damage for this skirmish are of no use to it; fierce until the regroup phase is.
		assertEquals("skirmish", scn.ShadowActionNoEffect(easterling, "strength"));
		assertEquals("skirmish", scn.ShadowActionNoEffect(easterling, "damage"));
		assertEquals("false", scn.ShadowActionNoEffect(easterling, "fierce"));

		var twilight = scn.GetTwilight();
		assertFalse(scn.HasKeyword(emboldened, Keyword.DAMAGE));
		scn.ShadowUseCardAction(easterling, "damage");
		assertEquals(twilight - 2, scn.GetTwilight());
		assertTrue(scn.HasKeyword(emboldened, Keyword.DAMAGE));

		scn.FreepsPass();
		assertFalse(scn.HasKeyword(emboldened, Keyword.FIERCE));
		scn.ShadowUseCardAction(easterling, "fierce");
		assertEquals(twilight - 4, scn.GetTwilight());
		assertTrue(scn.HasKeyword(emboldened, Keyword.FIERCE));

		// Aragorn loses, 8 against 10.  With the skirmish over the damage bonus is gone and fierce is not:
		// the lurker has a fierce assignment still to come.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.HasKeyword(emboldened, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(emboldened, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AWoundOutsideTheSkirmishIsNotFlagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var henneth = scn.GetFreepsCard("henneth");
		var henneth2 = scn.GetFreepsCard("henneth2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(henneth, henneth2);

		var goblinman = scn.GetShadowCard("goblinman");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(goblinman, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, goblinman);

		// At site 2 the minion fighting (site 2) is not roaming and the one left out (site 4) is.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, goblinman));
		assertFalse(scn.IsCharAssigned(runner));
		// A wound lands wherever its target is: it is no less a wound outside the skirmish.
		assertEquals("false", scn.FreepsActionNoEffect(henneth));

		scn.FreepsUseCardAction(henneth);
		assertInZone(Zone.DISCARD, henneth);
		// The one roaming minion took the wound, and had 1 vitality.
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(goblinman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// With no roaming minion left there is nobody at all for the wound, and the other copy is marked.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals("effect", scn.FreepsActionNoEffect(henneth2));

		scn.FreepsUseCardAction(henneth2);
		assertInZone(Zone.DISCARD, henneth2);
		assertEquals(0, scn.GetWoundsOn(goblinman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ADiscardOutsideTheSkirmishIsNotFlagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var warmed = scn.GetFreepsCard("warmed");
		var warmed2 = scn.GetFreepsCard("warmed2");
		// Sam is a Hobbit and Ring-bound: not one the event may discard.
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(warmed, warmed2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		// No unbound Hobbit is in play: nothing for the event to discard.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertEquals("effect", scn.FreepsActionNoEffect(warmed));

		scn.FreepsPlayCard(warmed);
		assertInZone(Zone.DISCARD, warmed);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// An unbound Hobbit arrives, who is not in the skirmish and does not have to be.
		scn.MoveCompanionsToTable(merry);
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.IsCharSkirmishing(merry));
		assertEquals("false", scn.FreepsActionNoEffect(warmed2));

		scn.FreepsPlayCard(warmed2);
		assertInZone(Zone.DISCARD, merry);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AStrengthPenaltyOrProtectionFromBeingOverwhelmedIsFlaggedWhileItReachesNobodyFighting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var golradir = scn.GetFreepsCard("golradir");
		var bounder = scn.GetFreepsCard("bounder");
		var bounder2 = scn.GetFreepsCard("bounder2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(golradir, bounder, bounder2);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{frodo, runner});
		scn.FreepsResolveSkirmish(aragorn);

		// A Man is fighting an Uruk-hai.  The one Orc, and every Hobbit, is outside the skirmish.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertEquals("skirmish", scn.FreepsActionNoEffect(golradir));
		assertEquals("skirmish", scn.FreepsActionNoEffect(bounder));
		assertEquals("skirmish", scn.FreepsActionNoEffect(bounder2));

		// Each may be used all the same.  The Orc is weaker for as long as this skirmish lasts.
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsUseCardAction(golradir);
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(5 - 1, scn.GetStrength(runner));
		scn.ShadowPass();
		scn.FreepsUseCardAction(bounder);
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(bounder));
		assertTrue(scn.FreepsHasCardChoiceAvailable(bounder2));
		scn.FreepsChooseCard(frodo);

		// Aragorn wins, 8 against 5.  In the skirmish of a Hobbit with an Orc neither ability is marked.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals("false", scn.FreepsActionNoEffect(golradir));
		assertEquals("false", scn.FreepsActionNoEffect(bounder2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void HobbitStealthAtAnyOtherSiteIsFlaggedWhileNoHobbitIsInTheSkirmishBeingFought() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOtherSitesScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var stealth = scn.GetFreepsCard("stealth");
		var stealth2 = scn.GetFreepsCard("stealth2");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(stealth, stealth2);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{sam, runner2});
		scn.FreepsResolveSkirmish(aragorn);

		// Not a site of the Fellowship block: the event strengthens a Hobbit, and cancels nothing.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(sam, runner2));
		assertEquals("skirmish", scn.FreepsActionNoEffect(stealth));

		scn.FreepsPlayCard(stealth);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam));
		scn.FreepsChooseCard(sam);
		assertEquals(3 + 2, scn.GetStrength(sam));
		assertInZone(Zone.DISCARD, stealth);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));

		// Aragorn wins, 8 against 5, and Sam's +2 is gone before his own skirmish begins.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner2));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals("false", scn.FreepsActionNoEffect(stealth2));

		scn.FreepsPlayCard(stealth2);
		scn.FreepsChooseCard(sam);
		assertEquals(3 + 2, scn.GetStrength(sam));
		// Nothing was cancelled.
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void APenaltyForEachMinionSkirmishingACharacterIsFlaggedOnlyWhileThereIsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var asfaloth = scn.GetFreepsCard("asfaloth");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(arwen, asfaloth);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2, savage);
		scn.SkipToAssignments();
		// The Uruk-hai is left out of every skirmish.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{arwen, runner2});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Nobody is skirmishing Arwen yet: "each minion skirmishing her" is no minion at all.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(arwen, runner2));
		assertTrue(scn.FreepsActionAvailable(asfaloth));
		assertEquals("effect", scn.FreepsActionNoEffect(asfaloth));

		// Aragorn wins, 8 against 5.  In Arwen's own skirmish the ability is not marked, with one minion
		// fighting her and another in play that is not.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner2));
		assertFalse(scn.IsCharSkirmishing(savage));
		assertEquals("false", scn.FreepsActionNoEffect(asfaloth));

		scn.FreepsUseCardAction(asfaloth);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(5 - 2, scn.GetStrength(runner2));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void APumpThatReachesNobodyFightingIsNotFlaggedWhenASecondEffectLands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var durability = scn.GetFreepsCard("durability");
		var durability2 = scn.GetFreepsCard("durability2");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(durability, durability2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		// The one Dwarf is not fighting, and Ettenmoors is neither a battleground nor a mountain: +2 to
		// him is all the event would do.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertFalse(scn.IsCharAssigned(gimli));
		assertEquals("skirmish", scn.FreepsActionNoEffect(durability));

		assertEquals(2, scn.GetFreepsHandCount());
		scn.FreepsPlayCard(durability);
		assertEquals(6 + 2, scn.GetStrength(gimli));
		// The event has left the hand and nothing was drawn.
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The site becomes a battleground: the event would now draw a card as well, which is something.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.BATTLEGROUND));
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals("false", scn.FreepsActionNoEffect(durability2));

		var deck = scn.GetFreepsDeckCount();
		scn.FreepsPlayCard(durability2);
		assertEquals(6 + 2 + 2, scn.GetStrength(gimli));
		// One card out of the hand and one drawn into it.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void APumpOfWhoeverPaysItsCostIsNotFlaggedThoughOnlyBystandersCanPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// Vitality 4 with 3 wounds: Aragorn is exhausted and cannot exert.
		scn.AddWoundsToChar(aragorn, 3);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		var ettenmoors = scn.GetCurrentSite();
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		// Gimli's own +2 is known to miss the skirmish.
		assertEquals("skirmish", scn.FreepsActionNoEffect(gimli));
		// The site's +2 goes to whichever companion is exerted for it.  Who that will be is the player's
		// choice, made as the cost is paid, so nothing is said of it: though here only companions outside
		// the skirmish can exert.
		assertTrue(scn.FreepsActionAvailable(ettenmoors));
		assertEquals("false", scn.FreepsActionNoEffect(ettenmoors));

		scn.FreepsUseCardAction(ettenmoors);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(6 + 2, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAbilityAtItsLimitIsOfferedFlaggedPaidForAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLimitScenario();

		// Two companions with Frodo, so that a second threat could be added if the limit did not hold.
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var tidings = scn.GetShadowCard("tidings");
		var axeman = scn.GetShadowCard("axeman");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCardsToSupportArea(tidings);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, axeman2);
		scn.SkipToPhase(Phase.REGROUP);
		// Enough for two uses and not for a third.
		scn.SetTwilight(7);
		scn.FreepsPass();

		// Within its limit the ability is not marked.
		assertTrue(scn.ShadowActionAvailable(tidings));
		assertEquals("false", scn.ShadowActionNoEffect(tidings));
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());

		scn.ShadowUseCardAction(tidings);
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		// "Limit 1" counts the threats added, not the uses: at its limit the ability has a cost that can
		// still be paid, so it is still offered, and is marked.
		assertTrue(scn.ShadowActionAvailable(tidings));
		assertEquals("limit", scn.ShadowActionNoEffect(tidings));

		scn.ShadowUseCardAction(tidings);
		// The cost is paid and no threat is added.
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		// With (1) left the cost cannot be paid, and that withholds it as it always did.
		assertFalse(scn.ShadowActionAvailable(tidings));
		assertTrue(scn.ShadowDecisionAvailable("Play Regroup action or Pass"));
	}

	@Test
	public void AnAbilityWithNoCostIsStillWithheldAtItsLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLimitScenario();

		// No Dwarf is in play, so the condition's other ability is never on offer.
		var liking = scn.GetFreepsCard("liking");
		scn.MoveCardsToSupportArea(liking);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(liking));
		assertEquals("false", scn.FreepsActionNoEffect(liking));
		assertEquals(0, scn.GetStackedCards(liking).size());

		scn.FreepsUseCardAction(liking);
		assertEquals(1, scn.GetStackedCards(liking).size());
		scn.ShadowPass();

		// Nothing is paid to stack a card, so at its limit the ability is not offered at all.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(liking));
	}

	@Test
	public void AnAbilityWithACostIsWithheldAtItsLimitOnUses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLimitScenario();

		var gardener = scn.GetFreepsCard("gardener");
		var sword = scn.GetFreepsCard("sword");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(gardener);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(gardener));
		assertEquals("false", scn.FreepsActionNoEffect(gardener));

		scn.FreepsUseCardAction(gardener);
		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(sword);
		assertEquals(1, scn.GetWoundsOn(gardener));
		assertInZone(Zone.HAND, sword);

		// "Limit once per turn" counts the uses.  Sam (vitality 4) could exert again and a [shire] item is
		// still in the draw deck, but the ability is not offered: there is nothing to flag.
		assertInZone(Zone.DECK, pipe);
		assertFalse(scn.IsExhausted(gardener));
		assertFalse(scn.FreepsActionAvailable(gardener));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnOptionalTriggerWithACostIsWithheldAtItsLimitOnUses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLimitScenario();

		var sam = scn.GetFreepsCard("sam");
		var shadowplay = scn.GetFreepsCard("shadowplay");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(shadowplay);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCardsToHand(troop, troop2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(troop);
		// Within its limit the trigger is offered and not marked.  Sam and Frodo can both exert, so the
		// Hobbit is chosen.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		assertEquals("false", scn.FreepsActionNoEffect(shadowplay));
		scn.FreepsChooseOptionalTrigger(shadowplay);
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(troop));

		// "Limit once per turn" counts the uses: when the next minion is played nothing is offered, though
		// a Hobbit could still exert.
		scn.ShadowPlayCard(troop2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABonusAtItsCapIsFlagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLimitScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, slayer);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(slayer));
		// Three uses, each +1, none of them marked.
		for (int used = 0; used < 3; used++) {
			assertEquals("false", scn.ShadowActionNoEffect(slayer));
			scn.ShadowUseCardAction(slayer);
			assertEquals(9 + used + 1, scn.GetStrength(slayer));
			scn.FreepsPass();
		}

		// At +3 the ability is still offered, and marked as at its limit.
		var twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(slayer));
		assertEquals("limit", scn.ShadowActionNoEffect(slayer));

		scn.ShadowUseCardAction(slayer);
		// (1) is removed and the minion is no stronger.
		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(9 + 3, scn.GetStrength(slayer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	// The cap is written into the amount of the bonus here, where Uruk Slayer's is written beside it.
	@Test
	public void ABonusWhoseAmountIsCappedIsFlaggedOnceTheCapIsSpent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLimitScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var realm = scn.GetFreepsCard("realm");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(realm);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// The skirmish is at site 2, Ettenmoors, which is not underground: strength is all the condition gives.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);

		assertEquals(6, scn.GetStrength(gimli));
		// Three uses, each +1, none of them marked.
		for (int used = 0; used < 3; used++) {
			assertEquals("false", scn.FreepsActionNoEffect(realm));
			scn.FreepsUseCardAction(realm);
			assertEquals(6 + used + 1, scn.GetStrength(gimli));
			scn.ShadowPass();
		}

		// At +3 the ability is still offered, and marked as at its limit.
		var deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(realm));
		assertEquals("limit", scn.FreepsActionNoEffect(realm));

		scn.FreepsUseCardAction(realm);
		// Three more cards are discarded and the Dwarf is no stronger.
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(6 + 3, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AKeywordWhoseAmountIsCappedIsFlaggedOnceTheCapIsSpent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLimitScenario();

		// Two companions with Frodo: room for two threats.
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals("false", scn.FreepsActionNoEffect(anduril));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(2, scn.GetThreatLimit());

		scn.FreepsUseCardAction(anduril);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		scn.ShadowPass();

		// At defender +1 the ability is still offered, and marked as at its limit.
		assertTrue(scn.FreepsActionAvailable(anduril));
		assertEquals("limit", scn.FreepsActionNoEffect(anduril));

		scn.FreepsUseCardAction(anduril);
		// A second threat is added and Aragorn is defender +1 still.
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.ShadowDecisionAvailable("Play Assignment action or Pass"));
	}

	protected VirtualTableScenario GetValueLimitScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// A definition of the tests' own (testCards.hjson): "Fellowship: Add (1) to make a companion
					// strength +1 (limit +2, or +4 if you can spot a Dwarf)."  Its cap is a value, not a number.
					put("banner", "0_8001");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ABonusWhoseCapIsAValueIsFlaggedOnceTheCapAsItStandsIsSpent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetValueLimitScenario();

		var banner = scn.GetFreepsCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(banner);

		scn.StartGame();

		assertEquals(8, scn.GetStrength(aragorn));
		// No Dwarf: the limit is +2.  Two uses, each +1, neither of them marked.
		for (int used = 0; used < 2; used++) {
			assertEquals("false", scn.FreepsActionNoEffect(banner));
			scn.FreepsUseCardAction(banner);
			scn.FreepsChooseCard(aragorn);
			assertEquals(8 + used + 1, scn.GetStrength(aragorn));
		}

		// At +2 the ability is still offered, and marked as at its limit: used, it costs its (1) and adds nothing.
		assertEquals("limit", scn.FreepsActionNoEffect(banner));
		assertEquals(2, scn.GetTwilight());
		scn.FreepsUseCardAction(banner);
		scn.FreepsChooseCard(aragorn);
		assertEquals(3, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals("limit", scn.FreepsActionNoEffect(banner));

		// With a Dwarf to spot the limit is +4, of which 2 are left: the mark goes.
		scn.MoveCompanionsToTable(gimli);
		scn.FreepsRefreshDecision();
		assertEquals("false", scn.FreepsActionNoEffect(banner));
		scn.FreepsUseCardAction(banner);
		scn.FreepsChooseCard(aragorn);
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals("false", scn.FreepsActionNoEffect(banner));
		scn.FreepsUseCardAction(banner);
		scn.FreepsChooseCard(aragorn);
		assertEquals(12, scn.GetStrength(aragorn));

		// And at +4 it is marked again.
		assertEquals("limit", scn.FreepsActionNoEffect(banner));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	protected VirtualTableScenario GetCapOnSomethingElseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sting: "Bearer must be Frodo.  Fellowship or Regroup: Exert Frodo to reveal an opponent's
					// hand.  Remove (1) for each Orc revealed (limit (4))."  Its cap is written into the amount of
					// twilight removed.
					put("sting", "1_313");
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc with no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: vitality 4
				VirtualTableScenario.RulingRing
		);
	}

	// A spent cap is read only where it is on a strength bonus or on the amount of a keyword (the tests above) or
	// on a number of cards drawn, and marks the action only when nothing else the action does would happen
	// either.  A cap on anything else is not read, and here the action still does something at its cap: the hand
	// is revealed.  It is not marked.
	@Test
	public void ACapOnSomethingElseAnEffectDoesIsNotFlagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCapOnSomethingElseScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"),
				scn.GetShadowCard("troop3"), scn.GetShadowCard("troop4"));

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();

		// Four Orcs revealed: (4) removed, which is the whole of the limit.
		assertEquals("false", scn.FreepsActionNoEffect(sting));
		scn.FreepsUseCardAction(sting);
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertEquals(6, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));

		// At the limit the ability is on offer and not marked.
		assertTrue(scn.FreepsActionAvailable(sting));
		assertEquals("false", scn.FreepsActionNoEffect(sting));

		// Used again, Frodo exerts and the hand is revealed, and nothing more is removed.
		scn.FreepsUseCardAction(sting);
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertEquals(6, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
